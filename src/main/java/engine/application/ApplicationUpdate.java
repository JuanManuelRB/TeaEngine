package engine.application;

import engine.Initializable;
import engine.Logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Update logic for the application.
 */
public abstract class ApplicationUpdate implements Logic, Initializable, AutoCloseable {
    private PreStep preStep;
    private Step[] steps;
    private PostStep postStep;

    public ApplicationUpdate(PreStep preStep, Step[] steps, PostStep postStep) {
        this.preStep = preStep;
        this.steps = steps;
        this.postStep = postStep;
    }

    public ApplicationUpdate(PreStep preStep, Step step, PostStep postStep, int stepUpdates) {
        Objects.requireNonNull(step);

        this.preStep = preStep;
        this.postStep = postStep;

        steps = new Step[stepUpdates];
        for (int i = 0; i < stepUpdates; i++)
            steps[i] = step;

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

    public ApplicationUpdate setSteps(Step[] steps) {
        this.steps = steps;
        return this;
    }

    public ApplicationUpdate addStep(Step step) {
        List<Step> steps = new ArrayList<>(List.of(this.steps));
        steps.add(step);
        this.steps = steps.toArray(new Step[0]);
        return this;
    }

    public ApplicationUpdate addSteps(Step[] steps) {
        List<Step> stepsList = new ArrayList<>(List.of(this.steps));
        stepsList.addAll(List.of(steps));
        this.steps = stepsList.toArray(new Step[0]);
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
