package cn.mythicland.mythiclobby.config.key;

public enum MessageKey implements ConfigKey {
    COMMAND_RELOAD_SUCCESS("command.reload.success"),
    COMMAND_SETSPAWN_FAILED("command.setspawn.failed"),
    COMMAND_SETSPAWN_SUCCESS("command.setspawn.success"),
    COMMAND_SPAWN_DISABLED("command.spawn.disabled"),
    COMMAND_SPAWN_SPAWN_DISABLED("command.spawn.spawn_disabled"),
    COMMAND_SPAWN_SPAWN_NOT_SET("command.spawn.spawn_not_set"),
    HOLOGRAM_CREATE_DUPLICATE_OR_INVALID("hologram.create.duplicate_or_invalid"),
    HOLOGRAM_CREATE_EMPTY_DISPLAY("hologram.create.empty_display"),
    HOLOGRAM_CREATE_INVALID_NAME("hologram.create.invalid_name"),
    HOLOGRAM_CREATE_SUCCESS("hologram.create.success"),
    HOLOGRAM_REMOVE_NOT_FOUND("hologram.remove.not_found"),
    HOLOGRAM_REMOVE_SUCCESS("hologram.remove.success"),
    MENU_OPEN_NO_PERMISSION("menu.open.no_permission"),
    MENU_OPEN_NOT_FOUND("menu.open.not_found"),
    NPC_CMD_ADD_SUCCESS("npc.cmd.add.success"),
    NPC_CMD_CLEAR_SUCCESS("npc.cmd.clear.success"),
    NPC_CMD_LIST_EMPTY("npc.cmd.list.empty"),
    NPC_CMD_LIST_ENTRY("npc.cmd.list.entry"),
    NPC_CMD_LIST_HEADER("npc.cmd.list.header"),
    NPC_CMD_REMOVE_OUT_OF_RANGE("npc.cmd.remove.out_of_range"),
    NPC_CMD_REMOVE_SUCCESS("npc.cmd.remove.success"),
    NPC_CREATE_FAILED("npc.create.failed"),
    NPC_CREATE_SUCCESS("npc.create.success"),
    NPC_REMOVE_SUCCESS("npc.remove.success"),
    NPC_SELECT_NONE_IN_SIGHT("npc.select.none_in_sight"),
    NPC_SELECT_NOT_FOUND("npc.select.not_found"),
    NPC_SELECT_REQUIRED("npc.select.required"),
    NPC_SELECT_SUCCESS("npc.select.success"),
    NPC_TPH_SUCCESS("npc.tph.success");

    private final String path;

    MessageKey(String path) {
        this.path = path;
    }

    @Override
    public String getPath() {
        return path;
    }
}
