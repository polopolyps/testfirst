package com.polopoly.ps.test;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import junit.framework.Assert;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import com.polopoly.model.ModelPathUtil;
import com.polopoly.ps.test.RenderOperationStorage.RenderOperation;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.util.CheckedCast;

public class ControllerAssertContext {
    private Class<? extends TestableController> controllerClass;

    private static final String LOCAL_PREFIX = "m.local.";
    private static final String GLOBAL_PREFIX = "m.";

    public ControllerAssertContext(Class<? extends TestableController> controller) {
        this.controllerClass = controller;
    }

    public <T> T getVariable(String variableName, Class<T> variableClass) throws Exception {
        return CheckedCast.cast(getVariable(variableName), variableClass, "Value of controller variable "
                + variableName);
    }

    public Object getVariable(String variableName) {
        if (variableName.startsWith(LOCAL_PREFIX)) {
            variableName = variableName.substring(LOCAL_PREFIX.length());
        }

        if (variableName.startsWith(GLOBAL_PREFIX)) {
            Assert.fail("Cannot test variables in any other scope than m.local.");
        }

        if (variableName.indexOf('.') != -1) {
            Assert.fail("Can only test variables, not use Velocity-style dot notation for subfields.");
        }

        Set<Class<?>> controllerClasses = new HashSet<Class<?>>();

        for (RenderOperation operation : RenderOperationStorage.getOperations()) {
            if (controllerClass.isAssignableFrom(operation.getController().getClass())) {
                TopModel model = operation.getModel();

                return ModelPathUtil.getBean(model.getLocal(), variableName);
            }

            controllerClasses.add(operation.getController().getClass());
        }

        Assert.fail("No controller of type " + controllerClass.getName() + " was executed. Known controllers were: "
                + controllerClasses + ".");

        return null;
    }

    public void assertVariableEquals(Object expected, String variableName) {
        Assert.assertEquals(expected, getVariable(variableName));
    }

    public void assertIterableVariableEquals(Iterable<?> exceptedIterable, String iterableVariableName) {
        Object variable = getVariable(iterableVariableName);
        Assert.assertTrue(variable instanceof Iterable<?>);
        Iterable<?> iterableVariable = (Iterable<?>) variable;
        Iterator<?> iteratorVariable = iterableVariable.iterator();
        for (Object exceptedObject : exceptedIterable) {
            Assert.assertTrue(iteratorVariable.hasNext());
            Object variableObject = iteratorVariable.next();
            Assert.assertEquals(exceptedObject, variableObject);
        }
        Assert.assertFalse(iteratorVariable.hasNext());
    }

    public void assertVelocityEquals(String expected, String velocityExpression) {
        Set<Class<?>> controllerClasses = new HashSet<Class<?>>();

        for (RenderOperation operation : RenderOperationStorage.getOperations()) {
            if (controllerClass.isAssignableFrom(operation.getController().getClass())) {
                TopModel model = operation.getModel();

                VelocityContext velocityContext = new VelocityContext();

                velocityContext.put("m", model);
                StringWriter writer = new StringWriter();

                try {
                    Velocity.evaluate(velocityContext, writer, "foo", new StringReader(velocityExpression));
                } catch (Exception e) {
                    e.printStackTrace();

                    Assert.fail(e.toString());
                }

                Assert.assertEquals(expected, writer.toString());
                return;
            }

            controllerClasses.add(operation.getController().getClass());
        }

        Assert.fail("No controller of type " + controllerClass.getName() + " was executed. Known controllers were: "
                + controllerClasses + ".");
    }
}