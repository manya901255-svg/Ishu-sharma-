using System;
using System.Collections.Generic;
using UnityEngine;

/// <summary>
/// LudoMatchGameManager: Manages Ludo match entry fees, table tiers, game stakes, and winnings.
/// Features:
/// - Definable table modes (Beginner, Amateur, Semi-Pro, Pro, High Roller).
/// - Validates wallet coin balance prior to match entry.
/// - Automatically deducts the entry fee upon match start.
/// - Awards 2x (double entry fee) to the winner upon match victory.
/// - Fires alerts/events on insufficient coin balance for UI dialogs/shop prompts.
/// - Fully synced with PlayerPrefs total coin storage.
/// </summary>
public class LudoMatchGameManager : MonoBehaviour
{
    public static LudoMatchGameManager Instance { get; private set; }

    [System.Serializable]
    public class TableTier
    {
        public string tierId;
        public string displayName;
        public int entryFee;
        public int winningReward; // Usually 2x entry fee for 2-player or customized for 4-player
        public int requiredPlayerCount = 2; // 2-player or 4-player match
        public Color tierColor = Color.white;
    }

    [Header("Table Modes Configuration")]
    public List<TableTier> tableTiers = new List<TableTier>()
    {
        new TableTier { tierId = "beginner", displayName = "Beginner Table", entryFee = 500, winningReward = 1000, requiredPlayerCount = 2, tierColor = new Color(0.2f, 0.8f, 0.3f) },
        new TableTier { tierId = "amateur",  displayName = "Amateur Lounge", entryFee = 2500, winningReward = 5000, requiredPlayerCount = 2, tierColor = new Color(0.2f, 0.6f, 0.9f) },
        new TableTier { tierId = "semipro",  displayName = "Club Master",    entryFee = 5000, winningReward = 10000, requiredPlayerCount = 2, tierColor = new Color(0.8f, 0.4f, 0.9f) },
        new TableTier { tierId = "pro",      displayName = "Pro Arena",      entryFee = 10000, winningReward = 20000, requiredPlayerCount = 2, tierColor = new Color(0.95f, 0.65f, 0.1f) },
        new TableTier { tierId = "highroller", displayName = "Royal Palace", entryFee = 50000, winningReward = 100000, requiredPlayerCount = 2, tierColor = new Color(0.9f, 0.2f, 0.2f) }
    };

    public enum MatchState
    {
        Lobby,
        MatchInProgress,
        MatchCompleted
    }

    private const string PREF_KEY_PLAYER_COINS = "Ludo_Player_TotalCoins";
    private const string PREF_KEY_MATCHES_PLAYED = "Ludo_Stats_MatchesPlayed";
    private const string PREF_KEY_MATCHES_WON = "Ludo_Stats_MatchesWon";

    public MatchState CurrentMatchState { get; private set; } = MatchState.Lobby;
    public TableTier ActiveTier { get; private set; }

    // Events for UI and Audio feedback
    public event Action<TableTier, long> OnMatchStarted;                    // (tier, remainingCoins)
    public event Action<TableTier, int, long> OnMatchWon;                  // (tier, rewardCoins, newBalance)
    public event Action<TableTier, long> OnMatchLost;                      // (tier, currentBalance)
    public event Action<TableTier, int, long> OnInsufficientBalanceAlert;  // (tier, requiredFee, currentBalance)
    public event Action<long> OnCoinsChanged;                              // (newBalance)

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

    /// <summary>
    /// Checks if the player can afford the entry fee for the given table mode.
    /// </summary>
    public bool HasEnoughCoinsForTable(TableTier tier)
    {
        if (tier == null) return false;
        return GetPlayerCoins() >= tier.entryFee;
    }

    /// <summary>
    /// Checks if the player can afford the table by its unique tier ID.
    /// </summary>
    public bool HasEnoughCoinsForTable(string tierId)
    {
        TableTier tier = GetTableTierById(tierId);
        return HasEnoughCoinsForTable(tier);
    }

    /// <summary>
    /// Starts a match with the specified table tier.
    /// Validates coins -> Deducts entry fee -> Transitions state -> Fires events.
    /// Returns true if match started successfully.
    /// </summary>
    public bool TryStartMatch(string tierId)
    {
        TableTier tier = GetTableTierById(tierId);
        if (tier == null)
        {
            Debug.LogError($"[LudoMatch] Table tier '{tierId}' not found!");
            return false;
        }

        return TryStartMatch(tier);
    }

    /// <summary>
    /// Starts a match with the specified table tier object.
    /// </summary>
    public bool TryStartMatch(TableTier tier)
    {
        if (tier == null)
        {
            Debug.LogError("[LudoMatch] Table tier cannot be null.");
            return false;
        }

        if (CurrentMatchState == MatchState.MatchInProgress)
        {
            Debug.LogWarning("[LudoMatch] A match is already in progress!");
            return false;
        }

        long currentCoins = GetPlayerCoins();

        // 1. Verify player has sufficient funds
        if (currentCoins < tier.entryFee)
        {
            Debug.LogWarning($"[LudoMatch] Insufficient coins! Required: {tier.entryFee}, Available: {currentCoins}");
            OnInsufficientBalanceAlert?.Invoke(tier, tier.entryFee, currentCoins);
            return false;
        }

        // 2. Deduct entry fee
        long remainingCoins = currentCoins - tier.entryFee;
        SetPlayerCoins(remainingCoins);

        // 3. Update match state and stats
        ActiveTier = tier;
        CurrentMatchState = MatchState.MatchInProgress;

        int totalPlayed = PlayerPrefs.GetInt(PREF_KEY_MATCHES_PLAYED, 0) + 1;
        PlayerPrefs.SetInt(PREF_KEY_MATCHES_PLAYED, totalPlayed);
        PlayerPrefs.Save();

        Debug.Log($"[LudoMatch] Match started on '{tier.displayName}'! Deducted entry fee {tier.entryFee}. Remaining: {remainingCoins}");

        OnMatchStarted?.Invoke(tier, remainingCoins);
        OnCoinsChanged?.Invoke(remainingCoins);
        return true;
    }

    /// <summary>
    /// Call when the local player wins the current match.
    /// Credits winning reward (2x entry fee) to their wallet.
    /// </summary>
    public void HandlePlayerVictory()
    {
        if (CurrentMatchState != MatchState.MatchInProgress || ActiveTier == null)
        {
            Debug.LogWarning("[LudoMatch] Cannot declare victory without an active match in progress.");
            return;
        }

        int reward = ActiveTier.winningReward;
        long newBalance = GetPlayerCoins() + reward;
        SetPlayerCoins(newBalance);

        int wins = PlayerPrefs.GetInt(PREF_KEY_MATCHES_WON, 0) + 1;
        PlayerPrefs.SetInt(PREF_KEY_MATCHES_WON, wins);
        PlayerPrefs.Save();

        CurrentMatchState = MatchState.MatchCompleted;
        Debug.Log($"[LudoMatch] VICTORY! Won {reward} coins on '{ActiveTier.displayName}'. New Balance: {newBalance}");

        OnMatchWon?.Invoke(ActiveTier, reward, newBalance);
        OnCoinsChanged?.Invoke(newBalance);
    }

    /// <summary>
    /// Call when the local player loses or forfeits the match.
    /// Entry fee was already deducted at start.
    /// </summary>
    public void HandlePlayerDefeat()
    {
        if (CurrentMatchState != MatchState.MatchInProgress || ActiveTier == null)
        {
            Debug.LogWarning("[LudoMatch] Cannot declare defeat without an active match in progress.");
            return;
        }

        CurrentMatchState = MatchState.MatchCompleted;
        long currentCoins = GetPlayerCoins();

        Debug.Log($"[LudoMatch] DEFEAT on '{ActiveTier.displayName}'. Better luck next time! Balance: {currentCoins}");

        OnMatchLost?.Invoke(ActiveTier, currentCoins);
    }

    /// <summary>
    /// Resets match state back to Lobby for next game selection.
    /// </summary>
    public void ReturnToLobby()
    {
        CurrentMatchState = MatchState.Lobby;
        ActiveTier = null;
    }

    // =========================================================================
    // HELPER & WALLET PERSISTENCE METHODS
    // =========================================================================

    public TableTier GetTableTierById(string tierId)
    {
        return tableTiers.Find(t => string.Equals(t.tierId, tierId, StringComparison.OrdinalIgnoreCase));
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
