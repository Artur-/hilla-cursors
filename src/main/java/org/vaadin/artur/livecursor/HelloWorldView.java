package org.vaadin.artur.livecursor;

import org.vaadin.artur.livecursor.service.CursorTrackerService;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;

@PageTitle("Hello World")
@Route("")
@AnonymousAllowed
@Menu(title = "Hello World")
public class HelloWorldView extends HorizontalLayout {

    private final TextField name;
    private final Button sayHello;

    public HelloWorldView(CursorTrackerService cursorTrackerService, MyCursor myCursor) {
        name = new TextField("Your name");
        name.setHelperText("Write your name here");
        sayHello = new Button("Say hello");
        sayHello.addClickListener(e -> {
            cursorTrackerService.updateName(myCursor.getId(), name.getValue());
            Notification.show("Hello " + name.getValue());
        });
        sayHello.addClickShortcut(Key.ENTER);

        setMargin(true);
        setVerticalComponentAlignment(Alignment.END, name, sayHello);

        add(name, sayHello);
        addClassNames(LumoUtility.Padding.MEDIUM, LumoUtility.Gap.MEDIUM);
    }
}
