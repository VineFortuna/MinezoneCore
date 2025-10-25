package anthony.SuperCraftBrawl.ranks;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public enum Rank {
    DEFAULT(0, "", color("&7")), //RANK GIVEN TO ALL NEW PLAYERS
    VIP(6, color("&a&lVIP"), color("&a")), //FIRST DONATION RANK
    PRO(8, color("&9&lPRO"), color("&9")), //SECOND DONATION RANK
    TRAINEE(3, color("&2&lTRAINEE"), color("&6")),
    /* Trainee:
     * Obtainable: Players who applied and have been accepted. They are under training and shall only have
     * Job: Learn from staff and helper players, with a little bit of chat moderation.
     * Limited permissions.
     */
    MODERATOR(4, color("&6&lMOD"), color("&6")),
    /*
     * Moderator:
     * Obtainable: Promotion from trainee (Passed final exam)
     * Job: Moderate gameplay chat and discord of the server to provide a safe place for players.
     */
    SR_MODERATOR(16,color("&6&lSR.MOD"), color("&6")),
    /*
     * SrMod:
     * Obtainable: Promotion from Moderator
     * Job: Appeals, Events, IP Punishments and community management
     */
    ADMIN(1, color("&c&lADMIN"), color("&c")),
    /*
     * Admin:
     * Obtainable: Applied or selected from SrMod
     * Job: Oversee their subsection they are assigned too (Appeals Events Ip punishments and community management)
     */
    DEVELOPER(5, color("&6&lDEV"), color("&6")),
    /*
     * Developer:
     * Obtainable: Application with a code test
     * Job: Implement new features for the server.
     */
    SUPERVISOR(7, "" + ChatColor.DARK_AQUA + ChatColor.BOLD + "SUPERVISOR", color("&b")),
    /*
     * Supervisor:
     * Obtainable: Admin being selected
     * LIMIT: only 1-3 Supervisors at a time
     * Job: Supervisors of all decisions on the servers and overseers of the admin team. They are LEADERSHIP
     */
    OWNER(2, color("&c&lOWNER"), color("&c")),
    /*
     * Owner:
     * Obtainable: NO
     * Job: Funding the server, and assisting in which events need assistance in.
     */
    QA(9, color("&a&lQA"), color("&a")),
    /*
     * QA:
     * Obtainable:
     * Job: Playtest early version of updates in the dev server and provide feedback
     *      on gameplay, mechanics, balance, and overall experience.
     */
    MEDIA(10,color("&b&lMEDIA"), color("&b")),
    /*
     * Media:
     * Obtainable: Application
     * Job: Advertise and record videos or streams for social medias.
     */
    PARTNER(11,color("&b&lPartner"), color("&b")),
    /*
     * Partner:
     * Obtainable: DEALS
     * Job: What ever the deal in which is what (A.K.A Enchilada)
     * NOT STAFF
     */
    STAFF_MANAGER(12,color("&4&lSTAFF MANAGER"), color("&4")),
    /*
     * Staff Manager:
     * Obtainable: Supervisor or Admin being selected by a Director+
     * Job: Management of all staff recuitment promotions demotions and fires.
     */
    DIRECTOR(13,color("&c&lDirector"), color("&c")),
    /*
     * Director:
     * Obtainable: Admin, leadership or Staff Manager Promotion/selection
     * Job: Director of their subsection (Community Management, Appeals, QA ect) (Overseers)
     */
    BUILDER(14, color("&2&lBUILDER"), color("&2")),
    /*toh
     * Builder:
     * Obtainable: Application
     * Job: Build and design new maps
     */
    SUPREME(17, color("&5&lSUPREME"), color("&5")),
    
    HR(18, color("&5&lHR"), color("&5"));
	/*
	 * Human Resources:
	 * Job: Oversee both the community and staff, ensuring smooth operations across the board
	 */

    private final int roleID;
    private final String tag;
    private final String arrowColor;

    Rank(int roleID, String tag, String arrowColor) {
        this.roleID = roleID; //CONSTRUCTOR
        this.tag = tag;
        this.arrowColor = arrowColor;
    }
    
    private static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public int getRoleID() { //ROLE ID
        return roleID;
    }

    public String getTag() { //TAG
        return tag;
    }
    
    public String getArrowColor() {
    	return arrowColor;
    }

    public String getTagWithSpace() {
        if (this == DEFAULT)
            return "";   //GIVES TAG A SPACE;
        else
            return tag  + ChatColor.RESET + " ";
    }

    public static Rank getRankFromID(int id) {
        for (Rank rank : Rank.values()) {
            if (rank.getRoleID() == id) {
                return rank; //ATTEMPTS TO GET A RANK WITH ID
            }
        }
        return Rank.DEFAULT;
    }
    
    public String getColorForNames(Player player, Rank rank) {
		String msg = "";

		if (rank == Rank.OWNER || rank == Rank.ADMIN)
			msg = color("&c");
		else if (rank == Rank.PRO)
			msg = color("&9");
		else if (rank == Rank.VIP)
			msg = color("&a");
        else if (rank == Rank.QA)
            msg = color("&a");
        else if (rank == Rank.DEVELOPER)
            msg = color("&6");
        else if (rank == Rank.MODERATOR)
            msg = color("&6");
        else if (rank == Rank.BUILDER)
            msg = color("&2");
		else
			msg = color("&7");

		return msg + player.getDisplayName();
	}

    public int getTabListIndex() {
        switch (this) {
            case OWNER:         return 0;
            case ADMIN:         return 1;
            case DEVELOPER:     return 2;
            case SR_MODERATOR:  return 3;
            case MODERATOR:     return 4;
            case TRAINEE:       return 5;
            case QA:            return 6;
            case BUILDER:       return 7;
            case MEDIA:         return 8;
            case SUPREME:       return 9;
            case PRO:       return 10;
            case VIP:           return 11;
            case DEFAULT:
            default:            return 12;
        }
    }

    public static Rank getRankFromName(String name) {
        for (Rank rank : Rank.values()) {
            if (rank.toString().equalsIgnoreCase(name)) { //ATTEMPTS TO GET A RANK WITH NAME
                return rank;
            }
        }
        return Rank.DEFAULT;
    }
}