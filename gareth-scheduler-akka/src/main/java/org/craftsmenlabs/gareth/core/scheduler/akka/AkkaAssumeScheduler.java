package org.craftsmenlabs.gareth.core.scheduler.akka;

import akka.actor.ActorSystem;
import org.craftsmenlabs.gareth.api.ExperimentEngine;
import org.craftsmenlabs.gareth.api.context.ExperimentContext;
import org.craftsmenlabs.gareth.api.context.ExperimentRunContext;
import org.craftsmenlabs.gareth.api.exception.GarethInvocationException;
import org.craftsmenlabs.gareth.api.exception.GarethUnknownDefinitionException;
import org.craftsmenlabs.gareth.api.invoker.MethodInvoker;
import org.craftsmenlabs.gareth.api.observer.Observer;
import org.craftsmenlabs.gareth.api.scheduler.AssumeScheduler;
import org.craftsmenlabs.gareth.core.invoker.MethodInvokerImpl;
import org.craftsmenlabs.gareth.core.reflection.DefinitionFactory;
import org.craftsmenlabs.gareth.core.reflection.ReflectionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * Created by hylke on 14/08/15.
 */
public class AkkaAssumeScheduler implements AssumeScheduler {

    private static final Logger logger = LoggerFactory.getLogger(AkkaAssumeScheduler.class);


    private final ActorSystem actorSystem;

    private final MethodInvoker methodInvoker;

    private final boolean ignoreInvocationExceptions;

    private final Observer observer;

    public AkkaAssumeScheduler(final Builder builder) {
        this.observer = builder.observer;
        this.methodInvoker = builder.methodInvoker;
        this.actorSystem = builder.actorSystem;
        this.ignoreInvocationExceptions = builder.ignoreInvocationExceptions;
    }

    @Override
    public void schedule(final ExperimentRunContext experimentRunContext, final ExperimentEngine experimentEngine) {
        final ExperimentContext experimentContext = experimentRunContext.getExperimentContext();
        final Duration time = experimentContext.getTime();
        try {
            actorSystem.scheduler().scheduleOnce(scala.concurrent.duration.Duration
                    .create(time.toMillis(), TimeUnit.MILLISECONDS), () -> {
                try {
                    logger.debug("Invoking assumption");
                    methodInvoker.invoke(experimentContext.getAssume());
                    experimentRunContext.setAssumeRun(LocalDateTime.now());
                    if (null != experimentContext.getSuccess()) {
                        logger.debug("Invoking success");
                        methodInvoker.invoke(experimentContext.getSuccess());
                        experimentRunContext.setSuccessRun(LocalDateTime.now());
                    }
                } catch (final Exception e) {
                    if (null != experimentContext.getFailure()) {
                        logger.debug("Invoking failure");
                        methodInvoker.invoke(experimentContext.getFailure());
                        experimentRunContext.setFailureRun(LocalDateTime.now());
                    }
                }
                observer.notifyApplicationStateChanged(experimentEngine);
            }, actorSystem.dispatcher());

        } catch (final GarethUnknownDefinitionException | GarethInvocationException e) {
            logger.error("Problem during assumption invocation", e);
            if (!ignoreInvocationExceptions) {
                throw e;
            }
        }
    }

    public static class Builder {

        private final Observer observer;

        private ReflectionHelper reflectionHelper;

        private MethodInvoker methodInvoker = new MethodInvokerImpl(reflectionHelper);

        private ActorSystem actorSystem = ActorSystem.create();

        private boolean ignoreInvocationExceptions;

        private DefinitionFactory customDefinitionFactory;

        public Builder(final Observer observer) {
            this.observer = observer;
        }

        public Builder setActorSystem(final ActorSystem actorSystem) {
            this.actorSystem = actorSystem;
            return this;
        }

        public Builder setIgnoreInvocationExceptions(final boolean ignoreInvocationExceptions) {
            this.ignoreInvocationExceptions = ignoreInvocationExceptions;
            return this;
        }

        public Builder setMethodInvoker(final MethodInvoker methodInvoker) {
            this.methodInvoker = methodInvoker;
            return this;
        }

        public Builder addCustomDefinitionFactory(DefinitionFactory definitionFactory) {
            this.customDefinitionFactory = definitionFactory;
            return this;
        }

        public AssumeScheduler build() {
            if (null == observer) {
                throw new IllegalStateException("Observer cannot be null");
            }

            reflectionHelper = new ReflectionHelper(customDefinitionFactory);
            return new AkkaAssumeScheduler(this);
        }
    }
}
