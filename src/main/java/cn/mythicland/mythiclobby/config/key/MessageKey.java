package cn.mythicland.mythiclobby.config.key;

public enum MessageKey {
    COMMAND_RELOAD_SUCCESS("&a已重载配置。"),
    COMMAND_SETSPAWN_FAILED("&c大厅出生点设置失败。"),
    COMMAND_SETSPAWN_SUCCESS("&a已将当前位置设置为大厅出生点。"),
    COMMAND_SPAWN_DISABLED("&c大厅主配置未启用！"),
    COMMAND_SPAWN_SPAWN_DISABLED("&c大厅出生点功能未启用！"),
    COMMAND_SPAWN_SPAWN_NOT_SET("&c大厅出生点尚未设置。"),
    HOLOGRAM_CREATE_DUPLICATE_OR_INVALID("&c同名悬浮字已存在, 或输入内容无效。"),
    HOLOGRAM_CREATE_EMPTY_DISPLAY("&c悬浮字显示内容不能为空！"),
    HOLOGRAM_CREATE_INVALID_NAME("&c悬浮字名称不能为空, 且不能包含 ."),
    HOLOGRAM_CREATE_SUCCESS("&a悬浮字 &e{0} &a已在你当前位置创建。"),
    HOLOGRAM_REMOVE_NOT_FOUND("&c未找到名为 &e{0} &c的悬浮字。"),
    HOLOGRAM_REMOVE_SUCCESS("&a悬浮字 &e{0} &a已删除。"),
    MENU_OPEN_NO_PERMISSION("&c你没有权限打开这个菜单。"),
    MENU_OPEN_NOT_FOUND("&c未找到名为 &e{0} &c的菜单。"),
    NPC_CMD_ADD_SUCCESS("&a已为 NPC &e{0} &a添加命令: &f/{1}&a。"),
    NPC_CMD_CLEAR_SUCCESS("&a已清空 NPC &e{0} &a的命令, 共移除 &e{1} &a条。"),
    NPC_CMD_LIST_EMPTY("&7暂无绑定命令。"),
    NPC_CMD_LIST_ENTRY("&e{0}. &f/{1}"),
    NPC_CMD_LIST_HEADER("&aNPC &e{0} &a的命令列表:"),
    NPC_CMD_REMOVE_OUT_OF_RANGE("&c命令索引超出范围。"),
    NPC_CMD_REMOVE_SUCCESS("&a已删除 NPC &e{0} &a的第 &e{1} &a条命令。"),
    NPC_CREATE_FAILED("&cNPC 创建失败。"),
    NPC_CREATE_SUCCESS("&aNPC 已创建。"),
    NPC_REMOVE_SUCCESS("&aNPC &e{0} &a已删除。"),
    NPC_SELECT_NONE_IN_SIGHT("&c你视线前方10格内没有可选中的NPC。"),
    NPC_SELECT_NOT_FOUND("&c未找到名为 &e{0} &c的NPC。"),
    NPC_SELECT_REQUIRED("&c你还没有选中任何 NPC。"),
    NPC_SELECT_SUCCESS("&a已选中 NPC &e{0}&a。"),
    NPC_TPH_SUCCESS("&a已将 NPC &e{0} &a传送到你当前位置。");

    private final String template;

    MessageKey(String template) {
        this.template = template;
    }

    public String getTemplate() {
        return template;
    }
}
