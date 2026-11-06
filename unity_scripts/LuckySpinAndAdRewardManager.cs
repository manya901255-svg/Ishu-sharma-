using System;
using System.Collections;
using UnityEngine;

/// <summary>
/// LuckySpinAndAdRewardManager: Controls the Lucky Spin Wheel and Watch-Ad Reward systems.
/// Handles:
/// - Spin wheel logic with random coin prizes from 1,000 to 50,000 coins.
/// - 4-hour cooldown timer between free spins using UTC timestamps in PlayerPrefs.
/// - Watch-Ad rewarded video simulation granting 1,000 bonus coins on completion.
/// - Unified PlayerPrefs coin persistence compatible with all Ludo modules.
/// </summary>
public class LuckySpinAndAdRewardManager : MonoBehaviour
{
    public static LuckySpinAndAdRewardManager Instance { get; private set; }

    [Header("Lucky Spin Configuration")]
    [Tooltip("Prize slices available on the wheel (coins)")]
    public int[] spinPrizes = new int[]
    {
        1000,   // Slice 0
        2500,   // Slice 1
        5000,   // Slice 2
        7500,   // Slice 3
        10000,  // Slice 4
        15000,  // Slice 5
        25000,  // Slice 6
        50000   // Slice 7 (Jackpot)
    };

    [Tooltip("Hours required to wait between free spins")]
    public float cooldownHours = 4.0f;

    [Header("Rewarded Ad Configuration")]
    [Tooltip("Coins awarded per watched video ad")]
    public int adRewardCoins = 1000;

    [Tooltip("Simulated ad watch duration in seconds (for mock testing)")]
    public float simulatedAdDurationSeconds = 2.0f;

    // PlayerPrefs storage keys
    private const string PREF_KEY_PLAYER_COINS = "Ludo_Player_TotalCoins";
    private const string PREF_KEY_LAST_SPIN_TIME = "Ludo_LuckySpin_LastTimestamp";
    private const string PREF_KEY_TOTAL_ADS_WATCHED = "Ludo_Ads_Watched_Count";

    // Events for UI & Animations
    public event Action<int, int> OnSpinCompleted; // (prizeAmount, prizeIndex)
    public event Action<int> OnSpinStarted;         // (targetIndex)
    public event Action<int> OnAdRewardClaimed;     // (coinsGranted)
    public event Action<long> OnCoinsUpdated;       // (newTotalBalance)
    public event Action<string> OnStatusMessage;    // (message)

    public bool IsSpinning { get; private set; }
    public bool IsWatchingAd { get; private set; }

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

    // =========================================================================
    // LUCKY SPIN METHODS
    // =========================================================================

    /// <summary>
    /// Checks if the player can perform a free spin right now.
    /// </summary>
    public bool CanSpinFree()
    {
        string lastSpinTimeStr = PlayerPrefs.GetString(PREF_KEY_LAST_SPIN_TIME, string.Empty);
        if (string.IsNullOrEmpty(lastSpinTimeStr))
        {
            return true; // Never spun before
        }

        if (long.TryParse(lastSpinTimeStr, out long binaryDate))
        {
            DateTime lastSpinDate = DateTime.FromBinary(binaryDate);
            TimeSpan timeElapsed = DateTime.UtcNow - lastSpinDate;
            return timeElapsed.TotalHours >= cooldownHours;
        }

        return true;
    }

    /// <summary>
    /// Gets the remaining TimeSpan before the next free spin is unlocked.
    /// Returns TimeSpan.Zero if spin is currently available.
    /// </summary>
    public TimeSpan GetRemainingCooldown()
    {
        string lastSpinTimeStr = PlayerPrefs.GetString(PREF_KEY_LAST_SPIN_TIME, string.Empty);
        if (string.IsNullOrEmpty(lastSpinTimeStr))
        {
            return TimeSpan.Zero;
        }

        if (long.TryParse(lastSpinTimeStr, out long binaryDate))
        {
            DateTime lastSpinDate = DateTime.FromBinary(binaryDate);
            DateTime nextAvailableTime = lastSpinDate.AddHours(cooldownHours);
            TimeSpan remaining = nextAvailableTime - DateTime.UtcNow;

            return remaining > TimeSpan.Zero ? remaining : TimeSpan.Zero;
        }

        return TimeSpan.Zero;
    }

    /// <summary>
    /// Formats the remaining cooldown time as "HH:mm:ss" for UI display.
    /// </summary>
    public string GetFormattedCooldownText()
    {
        TimeSpan remaining = GetRemainingCooldown();
        if (remaining <= TimeSpan.Zero)
        {
            return "READY TO SPIN!";
        }
        return string.Format("{0:D2}:{1:D2}:{2:D2}", remaining.Hours, remaining.Minutes, remaining.Seconds);
    }

    /// <summary>
    /// Initiates a spin of the Lucky Wheel.
    /// Selects a random prize between 1,000 and 50,000 coins,
    /// saves the current timestamp, and credits the coins.
    /// </summary>
    public bool SpinWheel()
    {
        if (IsSpinning)
        {
            Debug.LogWarning("[LuckySpin] Wheel is already spinning!");
            return false;
        }

        if (!CanSpinFree())
        {
            string remainingStr = GetFormattedCooldownText();
            Debug.LogWarning($"[LuckySpin] Cooldown active. Next free spin in: {remainingStr}");
            OnStatusMessage?.Invoke($"Cooldown active! Available in {remainingStr}");
            return false;
        }

        // Randomly select prize slice (weighted or uniform)
        int winningIndex = UnityEngine.Random.Range(0, spinPrizes.Length);
        int prizeAmount = spinPrizes[winningIndex];

        // Record timestamp immediately
        PlayerPrefs.SetString(PREF_KEY_LAST_SPIN_TIME, DateTime.UtcNow.ToBinary().ToString());
        PlayerPrefs.Save();

        IsSpinning = true;
        OnSpinStarted?.Invoke(winningIndex);

        // Start coroutine to simulate wheel animation duration before delivering prize
        StartCoroutine(SimulateSpinRoutine(winningIndex, prizeAmount, 3.5f));
        return true;
    }

    private IEnumerator SimulateSpinRoutine(int winningIndex, int prizeAmount, float animationDuration)
    {
        yield return new WaitForSeconds(animationDuration);

        // Credit coins
        long totalCoins = GetPlayerCoins() + prizeAmount;
        SetPlayerCoins(totalCoins);

        IsSpinning = false;
        Debug.Log($"[LuckySpin] Spin complete! Won {prizeAmount} coins. Total balance: {totalCoins}");

        OnSpinCompleted?.Invoke(prizeAmount, winningIndex);
        OnCoinsUpdated?.Invoke(totalCoins);
        OnStatusMessage?.Invoke($"Congratulations! You won {prizeAmount:N0} Coins!");
    }

    // =========================================================================
    // WATCH-AD REWARD METHODS
    // =========================================================================

    /// <summary>
    /// Simulates a rewarded video ad display.
    /// In production, integrate Google AdMob / Unity Ads Show() callback here.
    /// </summary>
    public void WatchAdForBonusCoins()
    {
        if (IsWatchingAd)
        {
            Debug.LogWarning("[WatchAd] Ad already in progress.");
            return;
        }

        IsWatchingAd = true;
        OnStatusMessage?.Invoke("Playing video ad...");
        Debug.Log("[WatchAd] Showing rewarded video ad...");

        // Simulate video playback duration before calling the reward callback
        StartCoroutine(SimulateRewardedAdRoutine());
    }

    private IEnumerator SimulateRewardedAdRoutine()
    {
        yield return new WaitForSeconds(simulatedAdDurationSeconds);

        // Simulated success callback from Ad SDK
        HandleRewardedAdSuccess();
    }

    /// <summary>
    /// Callback executed when the rewarded ad is completed successfully.
    /// Grants 1,000 bonus coins and updates PlayerPrefs.
    /// </summary>
    public void HandleRewardedAdSuccess()
    {
        IsWatchingAd = false;

        // 1. Credit 1,000 coins
        long newBalance = GetPlayerCoins() + adRewardCoins;
        SetPlayerCoins(newBalance);

        // 2. Track total ads watched
        int watchedCount = PlayerPrefs.GetInt(PREF_KEY_TOTAL_ADS_WATCHED, 0) + 1;
        PlayerPrefs.SetInt(PREF_KEY_TOTAL_ADS_WATCHED, watchedCount);
        PlayerPrefs.Save();

        Debug.Log($"[WatchAd] Ad finished! Granted {adRewardCoins} coins. Total coins: {newBalance} (Ad #{watchedCount})");

        OnAdRewardClaimed?.Invoke(adRewardCoins);
        OnCoinsUpdated?.Invoke(newBalance);
        OnStatusMessage?.Invoke($"+{adRewardCoins:N0} Bonus Coins added to your wallet!");
    }

    // =========================================================================
    // WALLET PERSISTENCE METHODS
    // =========================================================================

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
