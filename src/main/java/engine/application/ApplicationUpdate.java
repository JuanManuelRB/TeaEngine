package engine.application;

import engine.Logic;
import engine.Starts;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Update logic for the application.
 */
public abstract class ApplicationUpdate implements Logic, Starts, AutoCloseable {
    private PreStep preStep;
    private List<Step> steps = new ArrayList<>();
    private PostStep postStep;

    public ApplicationUpdate(PreStep preStep, Step step, PostStep postStep, int stepUpdates) {
        Objects.requireNonNull(step);

        this.preStep = preStep;
        this.postStep = postStep;

        // Poblate steps with the same step logic.
        for (int i = 0; i < stepUpdates; i++)
            this.steps.add(step);
    }

    public ApplicationUpdate(PreStep preStep, Step step, PostStep postStep) {
        this(preStep, step, postStep, 1);
    }

    public ApplicationUpdate(Step step, int stepUpdates) {
        this(null, step, null, stepUpdates);
    }

    public ApplicationUpdate(Step step) {
        this(null, step, null, 1);
    }

    public ApplicationUpdate(PreStep preStep, List<Step> steps, PostStep postStep) {
        this.preStep = preStep;
        this.steps = steps;
        this.postStep = postStep;
    }

    @Override
    public void update() {
        preStep();

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            executor.submit(this::step);
        }

        postStep();
    }

    public ApplicationUpdate setPreStep(PreStep preStep) {
        this.preStep = preStep;
        return this;
    }

    public ApplicationUpdate setSteps(List<Step> steps) {
        this.steps = steps;
        return this;
    }

    public ApplicationUpdate addStep(Step step) {
        this.steps.add(step);
        return this;
    }

    public ApplicationUpdate addSteps(List<Step> steps) {
        this.steps.addAll(steps);
        return this;
    }

    public ApplicationUpdate setPostStep(PostStep postStep) {
        this.postStep = postStep;
        return this;
    }

    private void preStep() {
        if (preStep != null)
            preStep.preStep();
    }

    private void step() {
        for (Step step : steps) if (step != null)
            step.step();
    }

    private void postStep() {
        if (postStep != null)
            postStep.postStep();
    }
}
