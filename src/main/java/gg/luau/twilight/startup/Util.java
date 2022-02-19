package gg.luau.twilight.startup;

import net.dv8tion.jda.api.entities.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Util {
    /*
    private static final long NAPPERS_MANAGER = 777543061441478686L;
    private static final long NAPPERS_TRYOUT = 777543061441478686L;
    private static final long NAPPERS_PLAYER = 777543061441478686L;
    */

    /*
    private static final long PANDAS_MANAGER = 777543126528032798L;
    private static final long PANDAS_TRYOUT = 777543126528032798L;
    private static final long PANDAS_PLAYER = 777543126528032798L;
    */

    /*
    private static final long CORGIS_MANAGER = 804420437702213693L;
    private static final long CORGIS_TRYOUT = 804420437702213693L;
    private static final long CORGIS_PLAYER = 804420437702213693L;
    */

    /*
    private static final long BLOSSOMS_MANAGER = 849107689685647360L;
    private static final long BLOSSOMS_TRYOUT = 849107689685647360L;
    private static final long BLOSSOMS_PLAYER = 849107689685647360L;
    */

    public static final long DRAGONS_MANAGER = 777543173106434098L;
    public static final long DRAGONS_TRYOUT = 891487855334010901L; // TODO: change this to 746782373051629679
    public static final long DRAGONS_PLAYER = 746782373051629679L;
    private static final Logger LOGGER = LoggerFactory.getLogger(Util.class);

    private Util() { }

    public static boolean canParticipate(Member member) {
        return isDragonsPlayer(member) || isDragonsTryout(member);
    }

    public static boolean isDragonsManager(Member member) {
        final var managerRole = member.getJDA().getRoleById(DRAGONS_MANAGER);

        if (managerRole == null) {
            LOGGER.error("Dragons manager role not in cache.");
            return false;
        }

        return member.getRoles().contains(managerRole);
    }

    public static boolean isDragonsTryout(Member member) {
        final var tryoutRole = member.getJDA().getRoleById(DRAGONS_TRYOUT);

        if (tryoutRole == null) {
            LOGGER.error("Dragons tryout role not in cache.");
            return false;
        }

        return member.getRoles().contains(tryoutRole);
    }

    public static boolean isDragonsPlayer(Member member) {
        final var playerRole = member.getJDA().getRoleById(DRAGONS_PLAYER);

        if (playerRole == null) {
            LOGGER.error("Dragons player role not in cache.");
            return false;
        }

        return member.getRoles().contains(playerRole);
    }
}
