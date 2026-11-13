package anthony.SuperCraftBrawl.Game;

public enum GameReason {
	SUCCESS,
    ALREADY_IN,
    IN_ANOTHER,
    ALREADYPLAYING,
    FULL,
    SPECTATOR,
    KICKED, // kicked from this custom game (they're told why)
    NO_ACCESS, // this private custom game isn't open to them (they're told why)
    FAIL;
}
