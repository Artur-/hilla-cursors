package org.vaadin.artur.hillacursor;

import org.springframework.stereotype.Component;

import com.vaadin.flow.spring.annotation.UIScope;

@Component
@UIScope
public class MyCursor {

    private String id;

    public MyCursor() {

    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
