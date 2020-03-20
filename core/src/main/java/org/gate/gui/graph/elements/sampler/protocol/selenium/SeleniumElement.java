package org.gate.gui.graph.elements.sampler.protocol.selenium;

import org.gate.gui.graph.elements.sampler.Sampler;

import java.util.List;

public interface SeleniumElement extends Sampler {

    List<String> getMethodSuppliers();
    String getCurrentMethodSupplier();
    void updateByMethodSupplier(String supplierName);
}
