package org.craftsmenlabs.gareth.rest.v1.assembler;

import org.craftsmenlabs.gareth.api.context.ExperimentContext;
import org.craftsmenlabs.gareth.rest.v1.entity.Experiment;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExperimentAssemblerTest {

    private ExperimentAssembler experimentAssembler;

    @Before
    public void setUp() throws Exception {
        experimentAssembler = new ExperimentAssembler();
    }

    @Test
    public void testAssembleOutbound() throws Exception {

        final ExperimentContext experimentContext = mock(ExperimentContext.class);
        when(experimentContext.getHash()).thenReturn("hash");
        when(experimentContext.getExperimentName()).thenReturn("experiment");
        when(experimentContext.getBaselineGlueLine()).thenReturn("baseline");
        when(experimentContext.getAssumeGlueLine()).thenReturn("assume");
        when(experimentContext.getTimeGlueLine()).thenReturn("time");
        when(experimentContext.getSuccessGlueLine()).thenReturn("success");
        when(experimentContext.getFailureGlueLine()).thenReturn("failure");

        final Experiment experiment = experimentAssembler.assembleOutbound(experimentContext);
        assertNotNull(experiment);
        assertEquals("hash", experiment.getHash());
        assertEquals("experiment", experiment.getExperimentName());
        assertEquals("baseline", experiment.getBaselineGlueLine());
        assertEquals("assume", experiment.getAssumeGlueLine());
        assertEquals("time", experiment.getTimeGlueLine());
        assertEquals("success", experiment.getSuccessGlueLine());
        assertEquals("failure", experiment.getFailureGlueLine());
    }

    @Test
    public void testAssembleOutboundWithNull() {
        assertNull(experimentAssembler.assembleOutbound(null));
    }


    @Test
    public void testAssembleInbound() throws Exception {
        try {
            final Experiment experiment = mock(Experiment.class);
            experimentAssembler.assembleInbound(experiment);
            fail("should not reach this point");
        } catch (final UnsupportedOperationException e) {
            assertTrue(e.getMessage().contains("Experiment cannot be assembled inbound"));
        }
    }
}