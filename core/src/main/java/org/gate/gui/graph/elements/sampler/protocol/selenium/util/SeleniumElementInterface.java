package org.gate.gui.graph.elements.sampler.protocol.selenium.util;

import org.gate.gui.common.TestElement;
import org.gate.gui.graph.elements.sampler.Sampler;

import java.util.List;

public interface SeleniumElementInterface extends TestElement {

    List<String> getMethodSuppliers();
    String getCurrentMethodSupplier();
    void updateByMethodSupplier(String supplierName);

}
