package org.comroid.listnr.model;

import org.comroid.listnr.Event;
import org.comroid.listnr.EventSender;
import org.comroid.listnr.HandlerManager;

public class BasicHandlerManager<T extends EventSender<T, ? extends E>, E extends Event<T>> implements HandlerManager<T, E> {}
