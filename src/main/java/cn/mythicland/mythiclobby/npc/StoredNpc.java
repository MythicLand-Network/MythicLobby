package cn.mythicland.mythiclobby.npc;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record StoredNpc(
        String name,
        List<String> displayLines,
        Location location,
        String skinPlayerName,
        List<String> commands
) {
    public StoredNpc(
            String name,
            List<String> displayLines,
            Location location,
            String skinPlayerName,
            List<String> commands
    ) {
        this.name = Objects.requireNonNull(name, "name");
        this.displayLines = new ArrayList<>(Objects.requireNonNull(displayLines, "displayLines"));
        this.location = Objects.requireNonNull(location, "location").clone();
        this.skinPlayerName = skinPlayerName;
        this.commands = new ArrayList<>(Objects.requireNonNull(commands, "commands"));
    }

    @Override
    public List<String> displayLines() {
        return new ArrayList<>(displayLines);
    }

    @Override
    public Location location() {
        return location.clone();
    }

    @Override
    public List<String> commands() {
        return new ArrayList<>(commands);
    }

    public String getName() {
        return name;
    }

    public List<String> getDisplayLines() {
        return displayLines();
    }

    public String getDisplayName() {
        if (displayLines.isEmpty()) {
            return name;
        }
        return displayLines.get(0);
    }

    public Location getLocation() {
        return location();
    }

    public String getSkinPlayerName() {
        return skinPlayerName;
    }

    public List<String> getCommands() {
        return commands();
    }

    public StoredNpc withLocation(Location location) {
        return new StoredNpc(name, displayLines, location, skinPlayerName, commands);
    }

    public StoredNpc withCommands(List<String> commands) {
        return new StoredNpc(name, displayLines, location, skinPlayerName, commands);
    }

    public StoredNpc addCommand(String command) {
        List<String> updatedCommands = new ArrayList<>(commands);
        updatedCommands.add(command);
        return withCommands(updatedCommands);
    }

    public StoredNpc removeCommand(int index) {
        List<String> updatedCommands = new ArrayList<>(commands);
        updatedCommands.remove(index);
        return withCommands(updatedCommands);
    }

    public StoredNpc clearCommands() {
        return withCommands(List.of());
    }
}
