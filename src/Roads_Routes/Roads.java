package Roads_Routes;

import java.util.List;

public class Roads {
    private static final String K = "k";
    private static final String SHARED = "shared";
    public Roads() {
    }
//  Zilina -> Strecno
    public static List<RoadPath> Z_S() {
        return List.of(
                new RoadPath("green_black", List.of(
                        new RoadSegment(RoadColor.GREEN, 4, false),
                        new RoadSegment(RoadColor.BLACK, 3, false)
                )),
                new RoadPath("red_red", List.of(
                        new RoadSegment(RoadColor.RED, 3, false),
                        new RoadSegment(RoadColor.RED, 4, false)
                )),
                new RoadPath(K, List.of(
                        new RoadSegment(RoadColor.BLACK, 2, false),
                        new RoadSegment(RoadColor.BLUE, 4, true)
                ))
        );
    }
//  Zilina -> Rajecke teplice
    public static List<RoadPath> Z_RT() {
        return List.of(
                new RoadPath(K, List.of(
                        new RoadSegment(RoadColor.BLACK, 2, false),
                        new RoadSegment(RoadColor.GREEN, 2, true)
                ))
        );
    }
//  Zilina -> Divinka
    public static List<RoadPath> Z_D() {
        return List.of(
                new RoadPath("green", List.of(
                        new RoadSegment(RoadColor.GREEN, 4, false)
                )),
                new RoadPath("red", List.of(
                        new RoadSegment(RoadColor.RED, 4, false)
                )),
                new RoadPath(K, List.of(
                        new RoadSegment(RoadColor.BLACK, 2, false),
                        new RoadSegment(RoadColor.RED, 2, true)
                ))
        );
    }
//  Strecno -> Zilina
    public static List<RoadPath> S_Z() {
        return List.of(
                new RoadPath("black_green", List.of(
                        new RoadSegment(RoadColor.BLACK, 3, false),
                        new RoadSegment(RoadColor.GREEN, 4, false)
                )),
                new RoadPath("red_red", List.of(
                        new RoadSegment(RoadColor.RED, 4, false),
                        new RoadSegment(RoadColor.RED, 3, false)
                )),
                new RoadPath(K, List.of(
                        new RoadSegment(RoadColor.BLUE, 4, false),
                        new RoadSegment(RoadColor.BLACK, 2, true)
                ))
        );
    }
//  Strecno -> Divinka
    public static List<RoadPath> S_D() {
        return List.of(
                new RoadPath(K, List.of(
                        new RoadSegment(RoadColor.BLUE, 4, false),
                        new RoadSegment(RoadColor.RED, 2, true)
                ))
        );
    }
//  Strecno -> Rajecke teplice
    public static List<RoadPath> S_RT() {
        return List.of(
                new RoadPath(SHARED, List.of(
                        new RoadSegment(RoadColor.BLUE, 8, false)
                )),
                new RoadPath("blue", List.of(
                        new RoadSegment(RoadColor.BLUE, 5, false)
                )),
                new RoadPath("black", List.of(
                        new RoadSegment(RoadColor.BLACK, 5, false)
                )),
                new RoadPath(K, List.of(
                        new RoadSegment(RoadColor.BLUE, 4, false),
                        new RoadSegment(RoadColor.GREEN, 2, true)
                ))
        );
    }
//  Rajecke teplice -> Strecno
    public static List<RoadPath> RT_S() {
        return List.of(
                new RoadPath(SHARED, List.of(
                        new RoadSegment(RoadColor.BLUE, 8, false)
                )),
                new RoadPath("blue", List.of(
                        new RoadSegment(RoadColor.BLUE, 5, false)
                )),
                new RoadPath("black", List.of(
                        new RoadSegment(RoadColor.BLACK, 5, false)
                )),
                new RoadPath(K, List.of(
                        new RoadSegment(RoadColor.GREEN, 2, false),
                        new RoadSegment(RoadColor.BLUE, 4, true)
                ))
        );
    }
//  Rajecke teplice -> Zilina
    public static List<RoadPath> RT_Z() {
        return List.of(
                new RoadPath(K, List.of(
                        new RoadSegment(RoadColor.GREEN, 2, false),
                        new RoadSegment(RoadColor.BLACK, 2, true)
                ))
        );
    }
//  Rajecke teplice -> Divinka
    public static List<RoadPath> RT_D() {
        return List.of(
                new RoadPath(SHARED, List.of(
                        new RoadSegment(RoadColor.BLUE, 1, false),
                        new RoadSegment(RoadColor.BLACK, 1, false)
                )),
                new RoadPath("red", List.of(
                        new RoadSegment(RoadColor.RED, 3, false)
                )),
                new RoadPath("red_blue", List.of(
                        new RoadSegment(RoadColor.RED, 2, false),
                        new RoadSegment(RoadColor.BLUE, 1, false)
                )),
                new RoadPath(K, List.of(
                        new RoadSegment(RoadColor.GREEN, 2, false),
                        new RoadSegment(RoadColor.RED, 2, true)
                ))
        );
    }
//  Divinka -> Rajecke teplice
    public static List<RoadPath> D_RT() {
        return List.of(
                new RoadPath(SHARED, List.of(
                        new RoadSegment(RoadColor.BLACK, 1, false),
                        new RoadSegment(RoadColor.BLUE, 1, false)
                )),
                new RoadPath("red", List.of(
                        new RoadSegment(RoadColor.RED, 3, false)
                )),
                new RoadPath("red_blue", List.of(
                        new RoadSegment(RoadColor.BLUE, 1, false),
                        new RoadSegment(RoadColor.RED, 2, false)
                )),
                new RoadPath(K, List.of(
                        new RoadSegment(RoadColor.RED, 2, false),
                        new RoadSegment(RoadColor.GREEN, 2, true)
                ))
        );
    }
//  Divinka -> Strecno
    public static List<RoadPath> D_S() {
        return List.of(
                new RoadPath(K, List.of(
                        new RoadSegment(RoadColor.RED, 2, false),
                        new RoadSegment(RoadColor.BLUE, 4, true)
                ))
        );
    }
//  Divinka -> Zilina
    public static List<RoadPath> D_Z() {
        return List.of(
                new RoadPath("green", List.of(
                        new RoadSegment(RoadColor.GREEN, 4, false)
                )),
                new RoadPath("red", List.of(
                        new RoadSegment(RoadColor.RED, 4, false)
                )),
                new RoadPath(K, List.of(
                        new RoadSegment(RoadColor.RED, 2, false),
                        new RoadSegment(RoadColor.BLACK, 2, true)
                ))
        );
    }
}
