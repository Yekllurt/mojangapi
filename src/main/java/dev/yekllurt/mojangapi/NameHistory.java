package dev.yekllurt.mojangapi;

import java.util.List;
import java.util.Objects;

public class NameHistory {

    private String initialName;
    private List<NameChange> nameChanges;

    public NameHistory(String initialName, List<NameChange> nameChanges) {
        Objects.requireNonNull(initialName);
        Objects.requireNonNull(nameChanges);
        this.initialName = initialName;
        this.nameChanges = nameChanges;
    }

    public String getCurrentName() {
        if (this.nameChanges.isEmpty()){
            return this.initialName;
        }
        return this.nameChanges.get(this.nameChanges.size() - 1).getName();
    }

    public List<NameChange> getNameChanges() {
        return this.nameChanges;
    }

    public String getInitialName() {
        return this.initialName;
    }
}
