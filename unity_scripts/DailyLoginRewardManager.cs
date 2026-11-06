using System;
using UnityEngine;

/// <summary>
/// DailyLoginRewardManager: Handles 7-day consecutive login rewards for a Unity Ludo game.
/// Tracks consecutive login days, saves the last claim timestamp using PlayerPrefs,
/// gives increasing coin rewards, and resets the streak if a day is missed.
/// </summary>
public class DailyLoginRewardManager : MonoBehaviour
{
    public static DailyLoginRewardManager Instance { get; private set; }

    [Header("Reward Configuration (Day 1 to Day 7)")]
    [Tooltip("Coins granted for Day 1 to Day 7 consecutively")]
    public int[] dailyCoinRewards = new int[7]
    {
        500,     // Day 1
        1200,    // Day 2
        2500,    // Day 3
        5000,    // Day 4
        10000,   // Day 5
        15000,   // Day 6
        25000    // Day 7
    };

    private const string PREF_KEY_STREAK_DAY = "Ludo_DailyLogin_StreakDay";
    private const string PREF_KEY_LAST_CLAIM_DATE = "Ludo_DailyLogin_LastDate";
    private const string PREF_KEY_PLAYER_COINS = "Ludo_Player_TotalCoins";

    // Events for UI feedback
    public event Action<int, int> OnRewardClaimed; // (streakDay, coinsEarned)
    public event Action OnStreakReset;
    public event Action<string> OnRewardStatusUpdated;

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
        CheckAndProcessStreak();
    }

    /// <summary>
    /// Gets the current streak day (1 to 7).
    /// </summary>
    public int GetCurrentStreakDay()
    {
        int day = PlayerPrefs.GetInt(PREF_KEY_STREAK_DAY, 1);
        return Mathf.Clamp(day, 1, 7);
    }

    /// <summary>
    /// Checks if the player can claim today's reward.
    /// Returns true if not claimed today yet.
    /// </summary>
    public bool CanClaimToday()
    {
        string lastClaimDateStr = PlayerPrefs.GetString(PREF_KEY_LAST_CLAIM_DATE, string.Empty);
        if (string.IsNullOrEmpty(lastClaimDateStr))
        {
            return true; // First time playing
        }

        if (DateTime.TryParse(lastClaimDateStr, out DateTime lastClaimDate))
        {
            DateTime today = DateTime.UtcNow.Date;
            return today > lastClaimDate.Date;
        }

        return true;
    }

    /// <summary>
    /// Verifies if consecutive streak was broken (missed more than 1 day).
    /// Resets streak to Day 1 if missed.
    /// </summary>
    public void CheckAndProcessStreak()
    {
        string lastClaimDateStr = PlayerPrefs.GetString(PREF_KEY_LAST_CLAIM_DATE, string.Empty);
        if (string.IsNullOrEmpty(lastClaimDateStr))
        {
            return;
        }

        if (DateTime.TryParse(lastClaimDateStr, out DateTime lastClaimDate))
        {
            DateTime today = DateTime.UtcNow.Date;
            int daysDifference = (today - lastClaimDate.Date).Days;

            // If 0 days -> already claimed today
            // If 1 day -> yesterday was claimed, streak intact
            // If >= 2 days -> missed yesterday! Reset streak
            if (daysDifference >= 2)
            {
                Debug.Log($"[DailyLogin] Player missed {daysDifference - 1} day(s). Streak reset to Day 1.");
                PlayerPrefs.SetInt(PREF_KEY_STREAK_DAY, 1);
                PlayerPrefs.Save();
                OnStreakReset?.Invoke();
            }
        }
    }

    /// <summary>
    /// Claims today's daily reward.
    /// Adds coins, updates streak day (advances 1 to 7, loops back to 1),
    /// and records today's date in PlayerPrefs.
    /// </summary>
    public bool ClaimTodayReward()
    {
        CheckAndProcessStreak();

        if (!CanClaimToday())
        {
            Debug.LogWarning("[DailyLogin] Reward already claimed for today! Come back tomorrow.");
            OnRewardStatusUpdated?.Invoke("Already claimed today!");
            return false;
        }

        int currentDay = GetCurrentStreakDay();
        int rewardCoins = dailyCoinRewards[currentDay - 1];

        // 1. Credit coins to player wallet
        long totalCoins = GetPlayerCoins();
        totalCoins += rewardCoins;
        SetPlayerCoins(totalCoins);

        // 2. Update last claim date to today
        DateTime today = DateTime.UtcNow.Date;
        PlayerPrefs.GetString(PREF_KEY_LAST_CLAIM_DATE, string.Empty);
        PlayerPrefs.SetString(PREF_KEY_LAST_CLAIM_DATE, today.ToString("yyyy-MM-dd"));

        // 3. Advance streak day for tomorrow (wrap 7 -> 1)
        int nextDay = (currentDay >= 7) ? 1 : currentDay + 1;
        PlayerPrefs.SetInt(PREF_KEY_STREAK_DAY, nextDay);
        PlayerPrefs.Save();

        Debug.Log($"[DailyLogin] Successfully claimed Day {currentDay} reward: +{rewardCoins} coins! Total: {totalCoins}");
        OnRewardClaimed?.Invoke(currentDay, rewardCoins);

        return true;
    }

    /// <summary>
    /// Gets time remaining until the next reward becomes available (at midnight UTC).
    /// </summary>
    public TimeSpan GetTimeToNextClaim()
    {
        DateTime now = DateTime.UtcNow;
        DateTime tomorrowMidnight = now.Date.AddDays(1);
        return tomorrowMidnight - now;
    }

    // --- Coin Wallet Helper Methods ---

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
