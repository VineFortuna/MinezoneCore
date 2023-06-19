package anthony.SuperCraftBrawl.ranks;

import org.bukkit.ChatColor;

public enum Rank {
    DEFAULT(0, ""), //RANK GIVEN TO ALL NEW PLAYERS
    VIP(6, "" + ChatColor.YELLOW + ChatColor.BOLD + "VIP"), //FIRST DONATION RANK
    CAPTAIN(8, "" + ChatColor.BLUE + ChatColor.BOLD + "CAPTAIN"), //SECOND DONATION RANK
    TRAINEE(3, "" + ChatColor.GOLD + ChatColor.BOLD + "TRAINEE"),
    //Trainee: Players who applied and have been accepted. They are under training and shall only have
    //Limited permissions. Job: Learn from staff and helper players, with a little bit of chat moderation.
    MODERATOR(4, "" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "MOD"),
    /*
     * Moderator:
     * Obtainable: Promotion from trainee (Passed final exam)
     * Job: Moderate gameplay chat and discord of the server to provide a safe place for players.
     * 
     */
    SENIOR_MODERATOR(16,color("&9&lSR.MOD")),
    /*
     * SrMod:
     * Obtainable: Promotion from Moderator
     * Job: Appeals, Events, IP Punishments and community management
     */
    ADMIN(1, "" + ChatColor.RED + ChatColor.BOLD + "ADMIN"),
    /*
     * Admin:
     * Obtainable: Applied or selected from SrMod
     * Job: Oversee their subsection they are assigned too (Appeals Events Ip punishments and community management)
     */
    DEVELOPER(5, "" + ChatColor.DARK_GREEN + ChatColor.BOLD + "DEV"),
    /*
     * Developer:
     * Obtainable: Application with a code test
     * Job: Design new features for the server.
     */
    SUPERVISOR(7, "" + ChatColor.DARK_AQUA + ChatColor.BOLD + "SUPERVISOR"),
    /*
     * Supervisor:
     * Obtainable: Admin being selected
     * LIMIT: only 1-3 Supervisors at a time
     * Job: Supervisors of all decisions on the servers and overseers of the admin team. They are LEADERSHIP
     */
    OWNER(2, "" + ChatColor.RED + ChatColor.BOLD + "OWNER"), 
    /*
     * Owner:
     * Obtainable: NO
     * LIMIT: 1
     * Job: Funding the server, and assisting in which events need assitance in. (Help build code or whatever)
     */
    QA(9, "" + ChatColor.YELLOW + ChatColor.BOLD + "QA"),
    /*
     * QA: SHOULD BE REMOVED
     */
    MEDIA(10,color("&b&lMEDIA")),
    /*
     * Media:
     * Obtainable: Application
     * Job: Advertise and record videos on tiktok insta or youtube
     */
    PARTNER(11,color("&b&lPARTNER")),
    /*
     * Partner:
     * Obtainable: DEALS
     * Job: What ever the deal in which is what (A.K.A Enchilada)
     *NOT STAFF
     */
    STAFF_MANAGER(12,color("&4&lSTAFF MANAGER")),
    /*
     * Staff Manager:
     * Obtainable: Supervisor or Admin being selected by a Director+
     * Job: Management of all staff recuitment promotions demotions and fires.
     */
    DIRECTOR(13,color("&c&lDIRECTOR")),
    /*
     * Director:
     * Obtainable: Admin, leadership or Staff Manager Promotion/selection
     * Job: Director of their subsection (Community Management, Appeals, QA ect) (Overseers)
     */
    BUILDER(14, color("&2&lBUILDER&r")),
    /*toh
     * Builder:
     * Obtainable: Application
     * Job: Build and design new maps
     */
    SUPREME(17, color("&5&lSUPREME")); //UNRELEASED DONATION ROLE ;)

    private final int roleID;
    private final String tag;

    Rank(int roleID, String tag) {
        this.roleID = roleID; //CONSTRUCTOR
        this.tag = tag;
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

    public int getTabListIndex(){
        //Lower numbers will show higher up in the tab list
        switch (this){
            case DEFAULT: return Rank.values().length;
            case OWNER: return 0;
            case ADMIN: return 1;
            case DIRECTOR: return 2;
            case DEVELOPER: return 3;
            case BUILDER: return 4;
            case MEDIA: return 5;
            case SUPERVISOR: return 6;
            case SENIOR_MODERATOR: return 7;
            case STAFF_MANAGER: return 8;
            case SUPREME: return 9;
            case MODERATOR: return 10;
            case QA: return 11;
            case CAPTAIN: return 12;
            case VIP: return 13;
            case PARTNER: return 14;
            case TRAINEE: return 15;
        }
        return 0;
    }

    public String getTagWithSpace() {
        if (this == DEFAULT)
            return "";   //GIVES TAG A SPACE;
        else
            return tag + " " + ChatColor.RESET;
    }

    public static Rank getRankFromID(int id) {
        for (Rank rank : Rank.values()) {
            if (rank.getRoleID() == id) {
                return rank; //ATTEMPTS TO GET A RANK WITH ID
            }
        }
        return Rank.DEFAULT;
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