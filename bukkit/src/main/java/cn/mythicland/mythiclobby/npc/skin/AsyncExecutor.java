package cn.mythicland.mythiclobby.npc.skin;

@FunctionalInterface
public interface AsyncExecutor {
    void execute(Runnable task);
}
