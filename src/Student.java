public class Student {

    private final String id;

    private String fullName;
    private String group;
    private List<Subject> debts;

    public Student(String id, String fullName, String group) {
        this(id, fullName, group, new ArrayList<Subject>());
    }

    public Student(String id,
                   String fullName,
                   String group,
                   List<Subject> debts) {
        this.id = id;
        this.fullName = fullName;
        this.group = group;
        this.debts = debts;
    }

    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public List<Subject> getDebts() {
        return debts;
    }

    public void setDebts(List<Subject> debts) {
        this.debts = debts;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}
