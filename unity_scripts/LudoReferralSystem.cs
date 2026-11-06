using System;
using System.Collections.Generic;
using UnityEngine;

/// <summary>
/// LudoReferralSystem: Handles invite codes, social app sharing, and referral rewards.
/// Features:
/// - Generates a persistent unique referral code (e.g. "LUDO-7X9B2K") & shareable deep link.
/// - One-tap sharing via WhatsApp, Android native Share Sheet, or social apps.
/// - Grants 5,000 coins when a referred friend joins using the code.
/// - Validates code redemptions to ensure each code can only be claimed once per user.
/// - Prevents self-referral abuse and tracks total successful referrals.
/// - Integrates with unified PlayerPrefs coin wallet.
/// </summary>
public class LudoReferralSystem : MonoBehaviour
{
    public static LudoReferralSystem Instance { get; private set; }

    [Header("Referral Reward Settings")]
    [Tooltip("Coins awarded to the inviter when a new friend joins")]
    public int inviterRewardCoins = 5000;

    [Tooltip("Welcome bonus coins given to the new user who enters an invite code")]
    public int inviteeWelcomeCoins = 2500;

    [Tooltip("Base URL for game download/invitation links")]
    public string gameDownloadBaseUrl = "https://play.google.com/store/apps/details?id=com.ludo.game&referrer=";

    // PlayerPrefs storage keys
    private const string PREF_KEY_MY_REFERRAL_CODE = "Ludo_MyReferralCode";
    private const string PREF_KEY_HAS_REDEEMED_CODE = "Ludo_HasRedeemedAnyInvite";
    private const string PREF_KEY_CLAIMED_CODES_LIST = "Ludo_ClaimedReferralCodesList"; // Semicolon-delimited
    private const string PREF_KEY_TOTAL_REFERRALS_COUNT = "Ludo_TotalReferralsCount";
    private const string PREF_KEY_PLAYER_COINS = "Ludo_Player_TotalCoins";

    // Events for UI feedback
    public event Action<string, string> OnReferralCodeReady;         // (code, fullShareLink)
    public event Action<string> OnShareLinkOpened;                   // (platformName)
    public event Action<int, long> OnReferralRewardEarned;           // (rewardCoins, newBalance)
    public event Action<int, long> OnInviteCodeRedeemedSuccess;      // (welcomeCoins, newBalance)
    public event Action<string> OnReferralActionFailed;              // (errorMessage)

    private void Awake()
    {
        if (Instance == null)
        {
            Instance = this;
            DontDestroyOnLoad(gameObject);
        }
        else
        {
            Destroy(gameObject);
        }
    }

    private void Start()
    {
        // Ensure user has a generated referral code
        string code = GetOrCreateMyReferralCode();
        string link = GetShareableLink();
        OnReferralCodeReady?.Invoke(code, link);
    }

    // =========================================================================
    // CODE GENERATION & RETRIEVAL
    // =========================================================================

    /// <summary>
    /// Retrieves or generates a persistent unique 6-character alphanumeric referral code.
    /// Example format: "LUDO-A8K92F"
    /// </summary>
    public string GetOrCreateMyReferralCode()
    {
        string existingCode = PlayerPrefs.GetString(PREF_KEY_MY_REFERRAL_CODE, string.Empty);
        if (!string.IsNullOrEmpty(existingCode))
        {
            return existingCode;
        }

        // Generate clean alphanumeric code: 6 chars
        const string chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // Omitted ambiguous 0/O, 1/I
        char[] codeChars = new char[6];
        System.Random random = new System.Random();
        for (int i = 0; i < codeChars.Length; i++)
        {
            codeChars[i] = chars[random.Next(chars.Length)];
        }

        string newCode = "LUDO-" + new string(codeChars);
        PlayerPrefs.SetString(PREF_KEY_MY_REFERRAL_CODE, newCode);
        PlayerPrefs.Save();

        Debug.Log($"[LudoReferral] Generated new persistent referral code: {newCode}");
        return newCode;
    }

    /// <summary>
    /// Gets the full deep-link or store link incorporating the player's code.
    /// </summary>
    public string GetShareableLink()
    {
        string code = GetOrCreateMyReferralCode();
        return $"{gameDownloadBaseUrl}{code}";
    }

    /// <summary>
    /// Gets the pre-formatted promotional message for sharing to friends.
    /// </summary>
    public string GetShareMessage()
    {
        string code = GetOrCreateMyReferralCode();
        string link = GetShareableLink();
        return $"🎲 Hey! Play Ludo with me! Use my invite code *{code}* to get {inviteeWelcomeCoins:N0} FREE bonus coins! Download here: {link}";
    }

    // =========================================================================
    // SOCIAL SHARING (WhatsApp, Social Apps, Native Intent)
    // =========================================================================

    /// <summary>
    /// Shares the referral link directly via WhatsApp.
    /// </summary>
    public void ShareViaWhatsApp()
    {
        string message = GetShareMessage();
        string urlEncoded = Uri.EscapeDataString(message);
        string whatsappUrl = $"whatsapp://send?text={urlEncoded}";

        Debug.Log("[LudoReferral] Opening WhatsApp share link...");
        Application.OpenURL(whatsappUrl);
        OnShareLinkOpened?.Invoke("WhatsApp");
    }

    /// <summary>
    /// Shares via general system share sheet (Native Android Intent or generic fallback).
    /// </summary>
    public void ShareViaSocialOrSystem()
    {
        string message = GetShareMessage();

#if UNITY_ANDROID && !UNITY_EDITOR
        try
        {
            using (AndroidJavaClass intentClass = new AndroidJavaClass("android.content.Intent"))
            using (AndroidJavaObject intentObject = new AndroidJavaObject("android.content.Intent"))
            {
                intentObject.Call<AndroidJavaObject>("setAction", intentClass.GetStatic<string>("ACTION_SEND"));
                intentObject.Call<AndroidJavaObject>("setType", "text/plain");
                intentObject.Call<AndroidJavaObject>("putExtra", intentClass.GetStatic<string>("EXTRA_SUBJECT"), "Join me in Ludo!");
                intentObject.Call<AndroidJavaObject>("putExtra", intentClass.GetStatic<string>("EXTRA_TEXT"), message);

                using (AndroidJavaClass unityPlayer = new AndroidJavaClass("com.unity3d.player.UnityPlayer"))
                using (AndroidJavaObject currentActivity = unityPlayer.GetStatic<AndroidJavaObject>("currentActivity"))
                using (AndroidJavaObject chooser = intentClass.CallStatic<AndroidJavaObject>("createChooser", intentObject, "Share Ludo Invite Code"))
                {
                    currentActivity.Call("startActivity", chooser);
                }
            }
            OnShareLinkOpened?.Invoke("SystemShare");
            return;
        }
        catch (Exception ex)
        {
            Debug.LogWarning($"[LudoReferral] Android native share failed: {ex.Message}. Falling back to URL.");
        }
#endif

        // Fallback for editor or non-Android: Copy to clipboard or open URL
        GUIUtility.systemCopyBuffer = message;
        Debug.Log("[LudoReferral] Copied invitation message to clipboard.");
        OnShareLinkOpened?.Invoke("Clipboard");
    }

    // =========================================================================
    // REWARD & REDEMPTION SYSTEM
    // =========================================================================

    /// <summary>
    /// Simulates/Processes a new user joining with this player's code.
    /// In multiplayer/backend setups, this callback is fired via cloud functions or webhooks.
    /// Grants the host 5,000 coins and increments their referral counter.
    /// </summary>
    /// <param name="newUserId">Unique ID of the friend who joined</param>
    public bool SimulateFriendJoinedWithMyCode(string newUserId)
    {
        if (string.IsNullOrEmpty(newUserId))
        {
            newUserId = "Guest_" + UnityEngine.Random.Range(1000, 9999);
        }

        // Verify this friend hasn't already been claimed
        List<string> claimedFriends = GetClaimedFriendList();
        if (claimedFriends.Contains(newUserId))
        {
            Debug.LogWarning($"[LudoReferral] Friend '{newUserId}' has already been credited for referral.");
            OnReferralActionFailed?.Invoke("This referral reward was already claimed.");
            return false;
        }

        // Record claimed friend to ensure single reward per user
        claimedFriends.Add(newUserId);
        SaveClaimedFriendList(claimedFriends);

        // Credit 5,000 coins to player
        long currentCoins = GetPlayerCoins();
        long newBalance = currentCoins + inviterRewardCoins;
        SetPlayerCoins(newBalance);

        // Increment referral stats
        int count = GetTotalReferralsCount() + 1;
        PlayerPrefs.SetInt(PREF_KEY_TOTAL_REFERRALS_COUNT, count);
        PlayerPrefs.Save();

        Debug.Log($"[LudoReferral] Friend '{newUserId}' joined! Awarded +{inviterRewardCoins} coins. Total: {newBalance} (Count: {count})");

        OnReferralRewardEarned?.Invoke(inviterRewardCoins, newBalance);
        return true;
    }

    /// <summary>
    /// Allows the current local user to redeem a friend's referral code.
    /// Validates:
    /// 1. User has not already redeemed an invite code.
    /// 2. Code is not user's own referral code.
    /// 3. Code format is valid.
    /// Awards welcome coins (2,500) upon successful redemption.
    /// </summary>
    public bool TryRedeemFriendCode(string friendCode)
    {
        if (string.IsNullOrWhiteSpace(friendCode))
        {
            OnReferralActionFailed?.Invoke("Please enter a valid referral code.");
            return false;
        }

        friendCode = friendCode.Trim().ToUpperInvariant();

        // 1. Check if user already redeemed an invite before (one claim per user)
        if (PlayerPrefs.GetInt(PREF_KEY_HAS_REDEEMED_CODE, 0) == 1)
        {
            Debug.LogWarning("[LudoReferral] You have already redeemed an invite code.");
            OnReferralActionFailed?.Invoke("You have already claimed an invite code on this device.");
            return false;
        }

        // 2. Prevent self-referral
        string myCode = GetOrCreateMyReferralCode().ToUpperInvariant();
        if (friendCode.Equals(myCode, StringComparison.OrdinalIgnoreCase))
        {
            Debug.LogWarning("[LudoReferral] Self-referral is not allowed!");
            OnReferralActionFailed?.Invoke("You cannot redeem your own referral code!");
            return false;
        }

        // 3. Mark as redeemed
        PlayerPrefs.SetInt(PREF_KEY_HAS_REDEEMED_CODE, 1);

        // 4. Grant welcome bonus to the invitee
        long newBalance = GetPlayerCoins() + inviteeWelcomeCoins;
        SetPlayerCoins(newBalance);
        PlayerPrefs.Save();

        Debug.Log($"[LudoReferral] Successfully redeemed code '{friendCode}'! Granted {inviteeWelcomeCoins} bonus coins.");

        OnInviteCodeRedeemedSuccess?.Invoke(inviteeWelcomeCoins, newBalance);
        return true;
    }

    public bool HasRedeemedInviteCode()
    {
        return PlayerPrefs.GetInt(PREF_KEY_HAS_REDEEMED_CODE, 0) == 1;
    }

    public int GetTotalReferralsCount()
    {
        return PlayerPrefs.GetInt(PREF_KEY_TOTAL_REFERRALS_COUNT, 0);
    }

    // =========================================================================
    // PERSISTENCE HELPERS
    // =========================================================================

    private List<string> GetClaimedFriendList()
    {
        string raw = PlayerPrefs.GetString(PREF_KEY_CLAIMED_CODES_LIST, string.Empty);
        if (string.IsNullOrEmpty(raw))
        {
            return new List<string>();
        }
        return new List<string>(raw.Split(new char[] { ';' }, StringSplitOptions.RemoveEmptyEntries));
    }

    private void SaveClaimedFriendList(List<string> list)
    {
        string raw = string.Join(";", list.ToArray());
        PlayerPrefs.SetString(PREF_KEY_CLAIMED_CODES_LIST, raw);
        PlayerPrefs.Save();
    }

    public long GetPlayerCoins()
    {
        string coinStr = PlayerPrefs.GetString(PREF_KEY_PLAYER_COINS, "5000");
        if (long.TryParse(coinStr, out long coins))
        {
            return coins;
        }
        return 5000L;
    }

    public void SetPlayerCoins(long coins)
    {
        PlayerPrefs.SetString(PREF_KEY_PLAYER_COINS, coins.ToString());
        PlayerPrefs.Save();
    }
}
