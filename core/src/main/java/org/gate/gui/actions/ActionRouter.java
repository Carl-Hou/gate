/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.gate.gui.actions;




import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gate.GateMain;
import org.gate.gui.common.OptionPane;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

public final class ActionRouter implements ActionListener {

    private static Logger log = LogManager.getLogger(GateMain.class);

    // This is cheap, so no need to resort to IODH or lazy compile
    private static final ActionRouter INSTANCE = new ActionRouter();

    private final Map<String, Set<Command>> commands = new HashMap<>();

    private final Map<String, Set<ActionListener>> preActionListeners =
            new HashMap<>();

    private final Map<String, Set<ActionListener>> postActionListeners =
            new HashMap<>();

    private ActionRouter() {
        addCommand(new Start());
        addCommand(new Save());
        addCommand(new Load());
        addCommand(new LoadRecentProject());
        addCommand(new New());
        addCommand(new AddToTree());
        addCommand(new EnableComponent());
        addCommand(new SSLManagerCommand());
        addCommand(new AddToGraph());
        addCommand(new LogLevel());
    }

    private void addCommand(Command command){
        HashSet<Command> actionCommands = new HashSet<>();
        actionCommands.add(command);
        for(String actionName : command.getActionNames()){
            commands.put(actionName, actionCommands);
        }
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                performAction(e);
            }
        });
    }

    private void performAction(final ActionEvent e) {
        String actionCommand = e.getActionCommand();
        try {
            for (Command c : commands.get(actionCommand)) {
                try {
                    preActionPerformed(c.getClass(), e);
                    c.doAction(e);
                    postActionPerformed(c.getClass(), e);
                } catch (IllegalUserActionException err) {
                    String msg = err.getMessage();
                    if (msg == null) {
                        msg = err.toString();
                    }
                    Throwable t = err.getCause();
                    if (t != null) {
                        String cause = t.getMessage();
                        if (cause == null) {
                            cause = t.toString();
                        }
                        msg = msg + "\n" + cause;
                    }
                    OptionPane.showErrorMessageDialog("ActionRouter" , msg);
                } catch (Exception err) {
                    log.error("Error processing "+c.toString(), err);
                }
            }
        } catch (NullPointerException er) {
            log.error("performAction(" + actionCommand + ") " + e.toString() + " caused", er);
            OptionPane.showErrorMessageDialog("ActionRouter" , "Sorry, " +
                    "this feature (" + actionCommand + ") not yet implemented");
        }
    }

    /**
     * To execute an action immediately in the current thread.
     *
     * @param e
     *            the action to execute
     */
    public void doActionNow(ActionEvent e) {
        performAction(e);
    }

    /**
     * Get the set of {@link Command}s registered under the name
     * <code>actionName</code>
     * 
     * @param actionName
     *            The name the {@link Command}s were registered
     * @return a set with all registered {@link Command}s for
     *         <code>actionName</code>
     */
    public Set<Command> getAction(String actionName) {
        Set<Command> set = new HashSet<>();
        for (Command c : commands.get(actionName)) {
            try {
                set.add(c);
            } catch (Exception err) {
                log.error("Could not add Command", err);
            }
        }
        return set;
    }

    /**
     * Get the {@link Command} registered under the name <code>actionName</code>,
     * that is of {@link Class} <code>actionClass</code>
     * 
     * @param actionName
     *            The name the {@link Command}s were registered
     * @param actionClass
     *            The class the {@link Command}s should be equal to
     * @return The registered {@link Command} for <code>actionName</code>, or
     *         <code>null</code> if none could be found
     */
    public Command getAction(String actionName, Class<?> actionClass) {
        for (Command com : commands.get(actionName)) {
            if (com.getClass().equals(actionClass)) {
                return com;
            }
        }
        return null;
    }

    /**
     * Get the {@link Command} registered under the name <code>actionName</code>
     * , which class names are equal to <code>className</code>
     * 
     * @param actionName
     *            The name the {@link Command}s were registered
     * @param className
     *            The name of the class the {@link Command}s should be equal to
     * @return The {@link Command} for <code>actionName</code> or
     *         <code>null</code> if none could be found
     */
    public Command getAction(String actionName, String className) {
        for (Command com : commands.get(actionName)) {
            if (com.getClass().getName().equals(className)) {
                return com;
            }
        }
        return null;
    }

    /**
     * Allows an ActionListener to receive notification of a command being
     * executed prior to the actual execution of the command.
     *
     * @param action
     *            the Class of the command for which the listener will
     *            notifications for. Class must extend
     *            org.apache.jmeter.gui.action.Command.
     * @param listener
     *            the ActionListener to receive the notifications
     */
    public void addPreActionListener(Class<?> action, ActionListener listener) {
        addActionListener(action, listener, preActionListeners);
    }

    /**
     * Allows an ActionListener to be removed from receiving notifications of a
     * command being executed prior to the actual execution of the command.
     *
     * @param action
     *            the Class of the command for which the listener will
     *            notifications for. Class must extend
     *            org.apache.jmeter.gui.action.Command.
     * @param listener
     *            the ActionListener to receive the notifications
     */
    public void removePreActionListener(Class<?> action, ActionListener listener) {
        removeActionListener(action, listener, preActionListeners);
    }

    /**
     * @param action {@link Class}
     * @param  {@link ActionListener}
     * @param actionListeners {@link Set}
     */
    private void removeActionListener(Class<?> action, ActionListener listener, Map<String, Set<ActionListener>> actionListeners) {
        if (action != null) {
            Set<ActionListener> set = actionListeners.get(action.getName());
            if (set != null) {
                set.remove(listener);
                actionListeners.put(action.getName(), set);
            }
        }
    }

    /**
     * Allows an ActionListener to receive notification of a command being
     * executed after the command has executed.
     *
     * @param action
     *            the Class of the command for which the listener will
     *            notifications for. Class must extend
     *            org.apache.jmeter.gui.action.Command.
     * @param listener
     *            The {@link ActionListener} to be registered
     */
    public void addPostActionListener(Class<?> action, ActionListener listener) {
        addActionListener(action, listener, postActionListeners);
    }

    /**
     * @param action {@link Class}
     * @param listener {@link ActionListener}
     * @param actionListeners {@link Set}
     */
    private void addActionListener(Class<?> action, ActionListener listener, Map<String, Set<ActionListener>> actionListeners) {
        if (action != null) {
            Set<ActionListener> set = actionListeners.get(action.getName());
            if (set == null) {
                set = new HashSet<>();
            }
            set.add(listener);
            actionListeners.put(action.getName(), set);
        }
    }

    /**
     * Allows an ActionListener to be removed from receiving notifications of a
     * command being executed after the command has executed.
     *
     * @param action
     *            the Class of the command for which the listener will
     *            notifications for. Class must extend
     *            org.apache.jmeter.gui.action.Command.
     * @param listener The {@link ActionListener} that should be deregistered
     */
    public void removePostActionListener(Class<?> action, ActionListener listener) {
        removeActionListener(action, listener, postActionListeners);
    }

    /**
     * @param action {@link Class}
     * @param e {@link ActionEvent}
     */
    protected void preActionPerformed(Class<? extends Command> action, ActionEvent e) {
        actionPerformed(action, e, preActionListeners);
    }

    /**
     * @param action {@link Class}
     * @param e {@link ActionEvent}
     */
    protected void postActionPerformed(Class<? extends Command> action, ActionEvent e) {
        actionPerformed(action, e, postActionListeners);
    }

    /**
     * @param action {@link Class}
     * @param e {@link ActionEvent}
     * @param actionListeners {@link Set}
     */
    private void actionPerformed(Class<? extends Command> action, ActionEvent e, Map<String, Set<ActionListener>> actionListeners) {
        if (action != null) {
            Set<ActionListener> listenerSet = actionListeners.get(action.getName());
            if (listenerSet != null && listenerSet.size() > 0) {
                ActionListener[] listeners = listenerSet.toArray(new ActionListener[listenerSet.size()]);
                for (ActionListener listener : listeners) {
                    listener.actionPerformed(e);
                }
            }
        }
    }

    /**
     * Gets the Instance attribute of the ActionRouter class
     *
     * @return The Instance value
     */
    public static ActionRouter getInstance() {
        return INSTANCE;
    }
}
