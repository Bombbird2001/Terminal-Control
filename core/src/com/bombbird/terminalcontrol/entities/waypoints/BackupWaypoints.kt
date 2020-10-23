package com.bombbird.terminalcontrol.entities.waypoints;

import java.util.HashMap;

public class BackupWaypoints {
    private static final HashMap<String, HashMap<String, int[]>> wptStorage = new HashMap<>();

    public static HashMap<String, int[]> loadBackupWpts(String mainName) {
        loadHashMap();
        return wptStorage.containsKey(mainName) ? wptStorage.get(mainName) : new HashMap<>();
    }

    private static void loadHashMap() {
        if (wptStorage.size() > 0) return;
        //Load old waypoints before waypoint overhaul
        //TCTP
        HashMap<String, int[]> tctp = new HashMap<>();
        tctp.put("CHALI", new int[] {1604, 938});
        tctp.put("BRAVO", new int[] {1787, 830});
        tctp.put("HLG", new int[] {1990, 608});
        tctp.put("JAMMY", new int[] {2211, 1307});
        tctp.put("FETUS", new int[] {2333, 1413});
        tctp.put("MARCH", new int[] {2455, 1520});
        tctp.put("APRIL", new int[] {2651, 1689});
        tctp.put("MAYOR", new int[] {2846, 1859});
        tctp.put("DECOY", new int[] {2503, 1863});
        tctp.put("JUNTA", new int[] {3043, 2029});
        tctp.put("JUROR", new int[] {3164, 2135});
        tctp.put("AUGUR", new int[] {3287, 2241});
        tctp.put("OCTAN", new int[] {2968, 2218});
        tctp.put("NEPAS", new int[] {3320, 2005});
        tctp.put("APU", new int[] {3391, 1808});
        tctp.put("SEPIA", new int[] {3485, 2411});
        tctp.put("CAROL", new int[] {3320, 2814});
        tctp.put("BAKER", new int[] {4021, 2715});
        tctp.put("ANNNA", new int[] {4272, 2576});
        tctp.put("PIANO", new int[] {3911, 3032});
        tctp.put("DUPAR", new int[] {3998, 1904});
        tctp.put("KUDOS", new int[] {4217, 1720});
        tctp.put("PINSI", new int[] {3948, 1547});
        tctp.put("SIDIN", new int[] {3812, 1359});
        tctp.put("SULIN", new int[] {3238, 1561});
        tctp.put("DRAKE", new int[] {4370, 2661});
        tctp.put("SITZE", new int[] {3621, 1576});
        tctp.put("ZONLI", new int[] {2835, 1387});
        tctp.put("PUTIN", new int[] {2465, 1047});
        tctp.put("RONEO", new int[] {2541, 827});
        tctp.put("FUSIN", new int[] {3341, 1194});
        tctp.put("YILAN", new int[] {3866, 959});
        tctp.put("TINHO", new int[] {4283, 204});
        tctp.put("XEBEC", new int[] {2883, 303});
        tctp.put("LU", new int[] {3882, 1735});
        tctp.put("D155V", new int[] {3743, 1188});
        tctp.put("COOKY", new int[] {3022, 1196});
        tctp.put("BESOM", new int[] {3982, 1654});
        tctp.put("ATOLL", new int[] {3657, 2044});
        tctp.put("SASHA", new int[] {3198, 1314});
        tctp.put("TSI", new int[] {3467, 1595});
        tctp.put("JONHO", new int[] {3025, 1307});
        tctp.put("TP050", new int[] {2976, 1714});
        tctp.put("TP060", new int[] {2988, 1687});
        tctp.put("TP064", new int[] {2985, 1684});
        tctp.put("TP230", new int[] {2773, 1538});
        tctp.put("TP240", new int[] {2788, 1516});
        tctp.put("NOVAS", new int[] {2698, 2031});
        tctp.put("SUMER", new int[] {2367, 1723});
        tctp.put("WUGOO", new int[] {3208, 1606});
        tctp.put("BITAN", new int[] {3438, 1339});
        tctp.put("APU30", new int[] {3076, 885});
        tctp.put("D242T", new int[] {2891, 1241});
        tctp.put("MUKKA", new int[] {3583, 1538});
        tctp.put("DICTA", new int[] {3196, 1609});
        tctp.put("VIVID", new int[] {3900, 2549});
        tctp.put("LOBAR", new int[] {3366, 1885});
        tctp.put("SENNA", new int[] {3646, 2175});
        tctp.put("GLEAM", new int[] {3591, 1594});
        tctp.put("TRINO", new int[] {3754, 1469});
        tctp.put("KUMAR", new int[] {3289, 1605});
        tctp.put("ROBIN", new int[] {4599, 2279});
        tctp.put("APU76", new int[] {1789, -63});
        tctp.put("MKG", new int[] {70, -1269});
        tctp.put("PABSO", new int[] {5759, 2707});
        tctp.put("KIKIT", new int[] {7038, 3180});
        tctp.put("MOLKA", new int[] {7756, 4690});
        tctp.put("LOTTO", new int[] {4840, 1660});
        tctp.put("WADER", new int[] {4118, 503});
        tctp.put("LARGO", new int[] {4188, 377});
        tctp.put("TAZAN", new int[] {3075, 1609});
        tctp.put("ITSG6.5", new int[] {3211, 1605});
        tctp.put("REFON", new int[] {3886, 1643});
        tctp.put("ITLU9", new int[] {3757, 1629});
        tctp.put("ITLU7", new int[] {3692, 1622});
        wptStorage.put("TCTP", tctp);

        //TCTT
        HashMap<String, int[]> tctt = new HashMap<>();
        tctt.put("CORGI", new int[] {3279, 1682});
        tctt.put("ALLAN", new int[] {3212, 1720});
        tctt.put("ATTAS", new int[] {3147, 1719});
        wptStorage.put("TCTT", tctt);

        //TCWS
        HashMap<String, int[]> tcws = new HashMap<>();
        tcws.put("TOPOM", new int[] {2980, 1891});
        tcws.put("DOKTA", new int[] {3246, 1767});
        tcws.put("DOGRA", new int[] {3367, 1097});
        tcws.put("DOSNO", new int[] {3360, 530});
        tcws.put("LEDOX", new int[] {2799, 1463});
        tcws.put("LETGO", new int[] {2765, 1381});
        tcws.put("DIVSA", new int[] {3000, 1280});
        tcws.put("BTM", new int[] {3159, 1187});
        tcws.put("TOKIM", new int[] {3006, 1879});
        tcws.put("IBIXU", new int[] {2825, 1451});
        tcws.put("IBIVA", new int[] {2791, 1370});
        tcws.put("DONDI", new int[] {2866, 1338});
        tcws.put("AGROT", new int[] {2840, 958});
        tcws.put("ABVIP", new int[] {2594, 926});
        tcws.put("ADMIM", new int[] {1946, 842});
        tcws.put("SAMKO", new int[] {2671, 1100});
        tcws.put("HOSBA", new int[] {3688, 1563});
        tcws.put("RUVIK", new int[] {3567, 1387});
        tcws.put("ATRUM", new int[] {2932, 1989});
        tcws.put("AKOMA", new int[] {2729, 2392});
        tcws.put("AKMET", new int[] {2371, 2669});
        tcws.put("AROSO", new int[] {1745, 3151});
        tcws.put("DOSPA", new int[] {3053, 1407});
        tcws.put("VTK", new int[] {2944, 1729});
        tcws.put("BOBAG", new int[] {1925, 1002});
        tcws.put("DOVAN", new int[] {3316, 1558});
        tcws.put("BIPOP", new int[] {3235, 1938});
        tcws.put("NYLON", new int[] {3108, 2119});
        tcws.put("BOKIP", new int[] {2378, 1062});
        tcws.put("LAVAX", new int[] {3784, 1240});
        tcws.put("IGNON", new int[] {3321, 1206});
        tcws.put("RUVIX", new int[] {3567, 1387});
        tcws.put("SANAT", new int[] {2885, 1175});
        tcws.put("IBULA", new int[] {4068, 617});
        tcws.put("LELIB", new int[] {1761, 1812});
        tcws.put("JB", new int[] {2340, 1894});
        tcws.put("ALFA", new int[] {2567, 1912});
        tcws.put("BIDUS", new int[] {2833, 2085});
        tcws.put("PASPU", new int[] {3105, 2842});
        tcws.put("POSUB", new int[] {3154, 1810});
        tcws.put("REMES", new int[] {2822, 393});
        tcws.put("SABKA", new int[] {1514, 2570});
        tcws.put("AGVAR", new int[] {2309, 2455});
        tcws.put("ANITO", new int[] {4586, -1575});
        tcws.put("ASUNA", new int[] {1277, 915});
        tcws.put("TOMAN", new int[] {6378, 1627});
        tcws.put("VENPA", new int[] {4519, -321});
        tcws.put("VMR", new int[] {2651, 3622});
        tcws.put("ATKAX", new int[] {8728, -855});
        tcws.put("BAVUS", new int[] {12625, -1024});
        tcws.put("MASBO", new int[] {724, 2957});
        tcws.put("VENIX", new int[] {6964, -1735});
        tcws.put("SURGA", new int[] {7805, -2222});
        tcws.put("KADAR", new int[] {10152, -1244});
        wptStorage.put("TCWS", tcws);
    }
}
