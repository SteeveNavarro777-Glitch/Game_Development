/* FILE PURPOSE: DSA GRAPH / MAP / SET | Prerequisites, completion membership and bounded journal deque.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
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
    public static final String[] BOSSES = {"tank", "mage", "fighter"};
    // [SECTION: DSA - DIRECTED PREREQUISITE GRAPH] Each quest stores its incoming requirements.
    private static final class Quest {
        final String label;
        final List<String> requires;
        // [BLOCK: FUNCTION] QuestLog.Quest - Initialize Quest state.
        Quest(String label, String... requires) {
            this.label = label; this.requires = List.of(requires);
        }
    }
    // [SECTION: DSA - LINKED HASH MAP] ID lookup preserves definition order; this is a library map.
    private final Map<String, Quest> quests = new LinkedHashMap<>();
    // [SECTION: DSA - HASH SET] Membership prevents duplicate completion rewards.
    private final Set<String> complete = new HashSet<>();
    // [SECTION: DSA - BOUNDED DEQUE] Newest-first journal; the pathfinder supplies the LIFO stack example.
    private final ArrayDeque<String> journal = new ArrayDeque<>();

    // [BLOCK: FUNCTION] QuestLog.QuestLog - Initialize QuestLog state.
    public QuestLog() {
        quests.put("grove", new Quest("Restore the Grove shrine"));
        quests.put("lake", new Quest("Restore the Lake shrine"));
        quests.put("ridge", new Quest("Restore the Ridge shrine"));
        quests.put("guardian", new Quest("Defeat the awakened guardian", SHRINES));
        quests.put("tank", new Quest("Defeat Tank in Clearing 1"));
        quests.put("mage", new Quest("Defeat Mage in Clearing 2"));
        quests.put("fighter", new Quest("Defeat Fighter in Clearing 3"));
        quests.put("home", new Quest("Return to the home beacon", "guardian", "tank", "mage", "fighter"));
        record("Restore the shrines, face the guardian and defeat the three villains.");
    }
    // [BLOCK: DSA: PREREQUISITE GRAPH] QuestLog.ready - Check that every required quest is complete before unlocking a quest.
    public boolean ready(String id) {
        Quest quest = quests.get(id);
        return quest != null && !done(id) && complete.containsAll(quest.requires);
    }
    // [BLOCK: OOP: ENCAPSULATION / HASH SET] QuestLog.finish - Complete a ready quest once and record it in the journal.
    public boolean finish(String id) {
        if (!ready(id)) return false;
        complete.add(id);
        record(quests.get(id).label + " - complete.");
        return true;
    }
    // [BLOCK: DSA: HASH SET] QuestLog.done - Test completion membership by quest ID.
    public boolean done(String id) { return complete.contains(id); }
    // [BLOCK: FUNCTION] QuestLog.seals - Handle seals for QuestLog.
    public int seals() {
        int count = 0;
        for (String id : SHRINES) if (done(id)) count++;
        return count;
    }
    // [BLOCK: DSA: BOUNDED DEQUE] QuestLog.record - Add the newest journal entry first and discard the oldest beyond eight entries.
    public void record(String text) {
        journal.addFirst(text);
        if (journal.size() > 8) journal.removeLast();
    }
    // [BLOCK: OOP: ENCAPSULATION] QuestLog.history - Return a copy of journal entries without exposing its deque.
    public List<String> history() { return new ArrayList<>(journal); }
}
