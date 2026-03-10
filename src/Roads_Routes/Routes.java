package Roads_Routes;

import java.util.List;

public class Routes {

    public static List<List<RoadPath>> Z_S_RT_D_Z() {
        return List.of(
                Roads.Z_S(),
                Roads.S_RT(),
                Roads.RT_D(),
                Roads.D_Z()
        );
    }

    public static List<List<RoadPath>> Z_S_D_RT_Z() {
        return List.of(
                Roads.Z_S(),
                Roads.S_D(),
                Roads.D_RT(),
                Roads.RT_Z()
        );
    }

    public static List<List<RoadPath>> Z_RT_S_D_Z() {
        return List.of(
                Roads.Z_RT(),
                Roads.RT_S(),
                Roads.S_D(),
                Roads.D_Z()
        );
    }

    public static List<List<RoadPath>> Z_RT_D_S_Z() {
        return List.of(
                Roads.Z_RT(),
                Roads.RT_D(),
                Roads.D_S(),
                Roads.S_Z()
        );
    }

    public static List<List<RoadPath>> Z_D_S_RT_Z() {
        return List.of(
                Roads.Z_D(),
                Roads.D_S(),
                Roads.S_RT(),
                Roads.RT_Z()
        );
    }

    public static List<List<RoadPath>> Z_D_RT_S_Z() {
        return List.of(
                Roads.Z_D(),
                Roads.D_RT(),
                Roads.RT_S(),
                Roads.S_Z()
        );
    }
}
