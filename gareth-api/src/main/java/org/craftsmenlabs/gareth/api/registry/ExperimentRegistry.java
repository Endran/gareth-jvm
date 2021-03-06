package org.craftsmenlabs.gareth.api.registry;

import org.craftsmenlabs.gareth.api.model.Experiment;

import java.util.List;

/**
 * Created by hylke on 11/08/15.
 */
public interface ExperimentRegistry {

    void addExperiment(final String experimentName, final Experiment experiment);

    Experiment getExperiment(final String experimentName);

    List<Experiment> getAllExperiments();
}
