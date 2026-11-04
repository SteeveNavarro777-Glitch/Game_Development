import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** A small prerequisite graph; three independent shrines unlock the guardian. */
public final class QuestLog {
    public static final String[] SHRINES = {"grove", "lake", "ridge"};
    private static final class Quest {
        final String label;
        final List<String> requires;
        Quest(String label, String... requires) {
            this.label = label; this.requires = List.of(requires);
        }
    }
    private final Map<String, Quest> quests = new LinkedHashMap<>();
    private final Set<String> complete = new HashSet<>();
    private final ArrayDeque<String> journal = new ArrayDeque<>();

    public QuestLog() {
        quests.put("grove", new Quest("Restore the Grove shrine"));
        quests.put("lake", new Quest("Restore the Lake shrine"));
        quests.put("ridge", new Quest("Restore the Ridge shrine"));
        quests.put("guardian", new Quest("Defeat the awakened guardian", SHRINES));
        quests.put("home", new Quest("Return to the home beacon", "guardian"));
        record("Restore the three shrines. Their seals awaken the guardian.");
    }
    public boolean ready(String id) {
        Quest quest = quests.get(id);
        return quest != null && !done(id) && complete.containsAll(quest.requires);
    }
    public boolean finish(String id) {
        if (!ready(id)) return false;
        complete.add(id);
        record(quests.get(id).label + " - complete.");
        return true;
    }
    public boolean done(String id) { return complete.contains(id); }
    public int seals() {
        int count = 0;
        for (String id : SHRINES) if (done(id)) count++;
        return count;
    }
    public void record(String text) {
        journal.addFirst(text);
        if (journal.size() > 8) journal.removeLast();
    }
    public List<String> history() { return new ArrayList<>(journal); }
    public String objective() {
        if (done("home")) return "The grove is safe. Agyu returns home.";
        if (done("guardian")) return "Return to the home beacon. Press E.";
        if (ready("guardian")) return "Find and defeat the guardian in the southeast.";
        return "Clear each shrine's guardians, then press E at its beacon.";
    }
}
