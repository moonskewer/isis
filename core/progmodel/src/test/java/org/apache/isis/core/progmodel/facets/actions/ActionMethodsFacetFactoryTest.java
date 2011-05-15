/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */


package org.apache.isis.core.progmodel.facets.actions;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.security.UserMemento;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.When;
import org.apache.isis.core.metamodel.facets.actions.choices.ActionChoicesFacet;
import org.apache.isis.core.metamodel.facets.actions.debug.DebugFacet;
import org.apache.isis.core.metamodel.facets.actions.defaults.ActionDefaultsFacet;
import org.apache.isis.core.metamodel.facets.actions.executed.ExecutedFacet;
import org.apache.isis.core.metamodel.facets.actions.exploration.ExplorationFacet;
import org.apache.isis.core.metamodel.facets.actions.invoke.ActionInvocationFacet;
import org.apache.isis.core.metamodel.facets.describedas.DescribedAsFacet;
import org.apache.isis.core.metamodel.facets.describedas.DescribedAsFacetAbstract;
import org.apache.isis.core.metamodel.facets.hide.HiddenFacet;
import org.apache.isis.core.metamodel.facets.named.NamedFacet;
import org.apache.isis.core.metamodel.facets.named.NamedFacetAbstract;
import org.apache.isis.core.metamodel.facets.param.choices.ActionParameterChoicesFacet;
import org.apache.isis.core.metamodel.facets.param.defaults.ActionParameterDefaultsFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.testspec.TestProxySpecification;
import org.apache.isis.core.progmodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.progmodel.facets.actions.defaults.method.ActionDefaultsFacetFactory;
import org.apache.isis.core.progmodel.facets.actions.defaults.method.ActionDefaultsFacetViaMethod;
import org.apache.isis.core.progmodel.facets.actions.invoke.ActionInvocationFacetFactory;
import org.apache.isis.core.progmodel.facets.actions.invoke.ActionInvocationFacetViaMethod;
import org.apache.isis.core.progmodel.facets.actions.validate.ActionValidationFacet;
import org.apache.isis.core.progmodel.facets.actions.validate.method.ActionValidationFacetViaMethod;
import org.apache.isis.core.progmodel.facets.actions.validate.method.ActionValidationFacetViaValidateMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.members.describedas.staticmethod.DescribedAsFacetViaDescriptionMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.members.disable.DisableForContextFacet;
import org.apache.isis.core.progmodel.facets.members.disable.DisableForSessionFacet;
import org.apache.isis.core.progmodel.facets.members.disable.DisabledFacet;
import org.apache.isis.core.progmodel.facets.members.disable.DisabledFacetAbstract;
import org.apache.isis.core.progmodel.facets.members.disable.forsession.DisableForSessionFacetViaMethod;
import org.apache.isis.core.progmodel.facets.members.disable.forsession.DisabledFacetViaDisableForSessionMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.members.disable.method.DisableForContextFacetViaMethod;
import org.apache.isis.core.progmodel.facets.members.disable.method.DisabledFacetViaDisableMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.members.disable.staticmethod.DisabledFacetViaProtectMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.members.hide.HiddenFacetAbstract;
import org.apache.isis.core.progmodel.facets.members.hide.HideForSessionFacet;
import org.apache.isis.core.progmodel.facets.members.hide.forsession.HiddenFacetViaHideForSessionMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.members.hide.forsession.HideForSessionFacetViaMethod;
import org.apache.isis.core.progmodel.facets.members.hide.staticmethod.HiddenFacetViaAlwaysHideMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.members.named.staticmethod.NamedFacetViaNameMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.param.choices.method.ActionChoicesFacetFactory;
import org.apache.isis.core.progmodel.facets.param.choices.method.ActionChoicesFacetViaMethod;
import org.apache.isis.core.progmodel.facets.param.choices.methodnum.ActionParameterChoicesFacetFactory;
import org.apache.isis.core.progmodel.facets.param.choices.methodnum.ActionParameterChoicesFacetViaMethod;
import org.apache.isis.core.progmodel.facets.param.defaults.methodnum.ActionParameterDefaultsFacetFactory;
import org.apache.isis.core.progmodel.facets.param.defaults.methodnum.ActionParameterDefaultsFacetViaMethod;


public class ActionMethodsFacetFactoryTest extends AbstractFacetFactoryTest {

    
    private final ObjectSpecification voidSpec = new TestProxySpecification("VOID");
    private final ObjectSpecification stringSpec = new TestProxySpecification("java.lang.String");
    private final ObjectSpecification customerSpec = new TestProxySpecification("Customer");

    
    public void testActionInvocationFacetIsInstalledAndMethodRemoved() {
        ActionInvocationFacetFactory facetFactory = new ActionInvocationFacetFactory();
        facetFactory.setSpecificationLookup(reflector);
        reflector.setLoadSpecificationStringReturn(voidSpec);

        class Customer {
            @SuppressWarnings("unused")
            public void someAction() {}
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.process(new ProcessMethodContext(Customer.class, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(ActionInvocationFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof ActionInvocationFacetViaMethod);
        final ActionInvocationFacetViaMethod actionInvocationFacetViaMethod = (ActionInvocationFacetViaMethod) facet;
        assertEquals(actionMethod, actionInvocationFacetViaMethod.getMethods().get(0));

        assertTrue(methodRemover.getRemoveMethodMethodCalls().contains(actionMethod));
    }

    public void testProvidesDefaultNameForActionButIgnoresAnyNamedAnnotation() {
        ActionInvocationFacetFactory facetFactory = new ActionInvocationFacetFactory();
        facetFactory.setSpecificationLookup(reflector);
        reflector.setLoadSpecificationStringReturn(voidSpec);
        
        class Customer {
            @SuppressWarnings("unused")
            @Named("Renamed an action with a named annotation")
            public void anActionWithNamedAnnotation() {}
        }
        final Method method = findMethod(Customer.class, "anActionWithNamedAnnotation");


        facetFactory.process(new ProcessMethodContext(Customer.class, method, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(NamedFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof NamedFacet);
        final NamedFacet namedFacet = (NamedFacet) facet;
        assertEquals("An Action With Named Annotation", namedFacet.value());
    }

    public void testPicksUpDebugPrefixAndSetsNameAppropriatelyAlso() {
        ActionInvocationFacetFactory facetFactory = new ActionInvocationFacetFactory();
        facetFactory.setSpecificationLookup(reflector);
        reflector.setLoadSpecificationStringReturn(voidSpec);
        
        class Customer {
            @SuppressWarnings("unused")
            public void debugAnActionWithDebugPrefix() {}
        }
        final Method method = findMethod(Customer.class, "debugAnActionWithDebugPrefix");
        facetFactory.process(new ProcessMethodContext(Customer.class, method, methodRemover, facetedMethod));

        Facet facet = facetedMethod.getFacet(DebugFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof DebugFacet);

        facet = facetedMethod.getFacet(NamedFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof NamedFacet);
        final NamedFacet namedFacet = (NamedFacet) facet;
        assertEquals("An Action With Debug Prefix", namedFacet.value());
    }

    public void testPicksUpExplorationPrefixAndSetsNameAppropriatelyAlso() {
        ActionInvocationFacetFactory facetFactory = new ActionInvocationFacetFactory();
        facetFactory.setSpecificationLookup(reflector);
        reflector.setLoadSpecificationStringReturn(voidSpec);
        
        class Customer {
            @SuppressWarnings("unused")
            public void explorationAnActionWithExplorationPrefix() {}
        }
        final Method method = findMethod(Customer.class, "explorationAnActionWithExplorationPrefix");
        facetFactory.process(new ProcessMethodContext(Customer.class, method, methodRemover, facetedMethod));

        Facet facet = facetedMethod.getFacet(ExplorationFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof ExplorationFacet);

        facet = facetedMethod.getFacet(NamedFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof NamedFacet);
        final NamedFacet namedFacet = (NamedFacet) facet;
        assertEquals("An Action With Exploration Prefix", namedFacet.value());
    }

    public void testCannotHaveBothDebugAndThenExplorationPrefix() {
        ActionInvocationFacetFactory facetFactory = new ActionInvocationFacetFactory();
        facetFactory.setSpecificationLookup(reflector);
        reflector.setLoadSpecificationStringReturn(voidSpec);
        
        class Customer {
            @SuppressWarnings("unused")
            public void debugExplorationAnActionWithDebugAndExplorationPrefix() {}
        }
        final Method method = findMethod(Customer.class, "debugExplorationAnActionWithDebugAndExplorationPrefix");
        facetFactory.process(new ProcessMethodContext(Customer.class, method, methodRemover, facetedMethod));

        Facet facet = facetedMethod.getFacet(DebugFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof DebugFacet);

        facet = facetedMethod.getFacet(ExplorationFacet.class);
        assertNull(facet);
    }

    public void testCannotHaveBothExplorationAndThenDebugPrefix() {
        ActionInvocationFacetFactory facetFactory = new ActionInvocationFacetFactory();
        facetFactory.setSpecificationLookup(reflector);
        reflector.setLoadSpecificationStringReturn(voidSpec);
        
        class Customer {
            @SuppressWarnings("unused")
            public void explorationDebugAnActionWithExplorationAndDebugPrefix() {}
        }
        final Method method = findMethod(Customer.class, "explorationDebugAnActionWithExplorationAndDebugPrefix");
        facetFactory.process(new ProcessMethodContext(Customer.class, method, methodRemover, facetedMethod));

        Facet facet = facetedMethod.getFacet(ExplorationFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof ExplorationFacet);

        facet = facetedMethod.getFacet(DebugFacet.class);
        assertNull(facet);
    }

    public void testPicksUpLocalPrefixAndSetsNameAppropriatelyAlso() {
        ActionInvocationFacetFactory facetFactory = new ActionInvocationFacetFactory();
        facetFactory.setSpecificationLookup(reflector);
        reflector.setLoadSpecificationStringReturn(voidSpec);

        class Customer {
            @SuppressWarnings("unused")
            public void localAnActionWithLocalPrefix() {}
        }
        final Method method = findMethod(Customer.class, "localAnActionWithLocalPrefix");
        facetFactory.process(new ProcessMethodContext(Customer.class, method, methodRemover, facetedMethod));

        Facet facet = facetedMethod.getFacet(ExecutedFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof ExecutedFacet);
        final ExecutedFacet executedFacet = (ExecutedFacet) facet;
        assertEquals(ExecutedFacet.Where.LOCALLY, executedFacet.value());

        facet = facetedMethod.getFacet(NamedFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof NamedFacet);
        final NamedFacet namedFacet = (NamedFacet) facet;
        assertEquals("An Action With Local Prefix", namedFacet.value());
    }

    public void testPicksUpRemotePrefixAndSetsNameAppropriatelyAlso() {
        ActionInvocationFacetFactory facetFactory = new ActionInvocationFacetFactory();
        facetFactory.setSpecificationLookup(reflector);
        reflector.setLoadSpecificationStringReturn(voidSpec);

        class Customer {
            @SuppressWarnings("unused")
            public void remoteAnActionWithRemotePrefix() {}
        }
        final Method method = findMethod(Customer.class, "remoteAnActionWithRemotePrefix");
        facetFactory.process(new ProcessMethodContext(Customer.class, method, methodRemover, facetedMethod));

        Facet facet = facetedMethod.getFacet(ExecutedFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof ExecutedFacet);
        final ExecutedFacet executedFacet = (ExecutedFacet) facet;
        assertEquals(ExecutedFacet.Where.REMOTELY, executedFacet.value());

        facet = facetedMethod.getFacet(NamedFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof NamedFacet);
        final NamedFacet namedFacet = (NamedFacet) facet;
        assertEquals("An Action With Remote Prefix", namedFacet.value());
    }

    public void testInstallsValidateMethodNoArgsFacetAndRemovesMethod() {
        ActionValidationFacetViaValidateMethodFacetFactory facetFactory = new ActionValidationFacetViaValidateMethodFacetFactory();
        facetFactory.setSpecificationLookup(reflector);
        reflector.setLoadSpecificationStringReturn(voidSpec);

        class Customer {
            @SuppressWarnings("unused")
            public void someAction() {}

            @SuppressWarnings("unused")
            public String validateSomeAction() {
                return null;
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");
        final Method validateMethod = findMethod(Customer.class, "validateSomeAction");

        facetFactory.process(new ProcessMethodContext(Customer.class, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(ActionValidationFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof ActionValidationFacetViaMethod);
        final ActionValidationFacetViaMethod actionValidationFacetViaMethod = (ActionValidationFacetViaMethod) facet;
        assertEquals(validateMethod, actionValidationFacetViaMethod.getMethods().get(0));

        assertTrue(methodRemover.getRemoveMethodMethodCalls().contains(validateMethod));
    }

    public void testInstallsValidateMethodSomeArgsFacetAndRemovesMethod() {
        ActionValidationFacetViaValidateMethodFacetFactory facetFactory = new ActionValidationFacetViaValidateMethodFacetFactory();
        facetFactory.setSpecificationLookup(reflector);
        reflector.setLoadSpecificationStringReturn(voidSpec);

        class Customer {
            @SuppressWarnings("unused")
            public void someAction(final int x, final int y) {}

            @SuppressWarnings("unused")
            public String validateSomeAction(final int x, final int y) {
                return null;
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction", new Class[] { int.class, int.class });
        final Method validateMethod = findMethod(Customer.class, "validateSomeAction", new Class[] { int.class, int.class });

        facetFactory.process(new ProcessMethodContext(Customer.class, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(ActionValidationFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof ActionValidationFacetViaMethod);
        final ActionValidationFacetViaMethod actionValidationFacetViaMethod = (ActionValidationFacetViaMethod) facet;
        assertEquals(validateMethod, actionValidationFacetViaMethod.getMethods().get(0));

        assertTrue(methodRemover.getRemoveMethodMethodCalls().contains(validateMethod));
    }

    public void testInstallsParameterDefaultsMethodNoArgsFacetAndRemovesMethod() {
        ActionDefaultsFacetFactory facetFactory = new ActionDefaultsFacetFactory();
        facetFactory.setSpecificationLookup(reflector);
        reflector.setLoadSpecificationStringReturn(voidSpec);
        
        class Customer {
            @SuppressWarnings("unused")
            public void someAction(final int x, final Long y) {}

            @SuppressWarnings("unused")
            public Object[] defaultSomeAction() {
                return null;
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction", new Class[] { int.class, Long.class });
        final Method defaultMethod = findMethod(Customer.class, "defaultSomeAction", new Class[] { });

        facetFactory.process(new ProcessMethodContext(Customer.class, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(ActionDefaultsFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof ActionDefaultsFacetViaMethod);
        final ActionDefaultsFacetViaMethod actionDefaultFacetViaMethod = (ActionDefaultsFacetViaMethod) facet;
        assertEquals(defaultMethod, actionDefaultFacetViaMethod.getMethods().get(0));

        assertTrue(methodRemover.getRemoveMethodMethodCalls().contains(defaultMethod));
    }

    public void testInstallsParameterDefaultsMethodSomeArgsIsIgnored() {
        ActionDefaultsFacetFactory facetFactory = new ActionDefaultsFacetFactory();
        facetFactory.setSpecificationLookup(reflector);
        reflector.setLoadSpecificationStringReturn(voidSpec);

        class Customer {
            @SuppressWarnings("unused")
            public void someAction(final int x, final Long y) {}

            @SuppressWarnings("unused")
            public Object[] defaultSomeAction( final int x, final Long y ) {
                return null;
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction", new Class[] { int.class, Long.class });

        facetFactory.process(new ProcessMethodContext(Customer.class, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(ActionDefaultsFacet.class);
        assertNull(facet);
    }

    public void testInstallsParameterChoicesMethodNoArgsFacetAndRemovesMethod() {
        ActionChoicesFacetFactory facetFactory = new ActionChoicesFacetFactory();
        facetFactory.setSpecificationLookup(reflector);

        class Customer {
            @SuppressWarnings("unused")
            public void someAction(final int x, final Long y) {}

            @SuppressWarnings("unused")
            public Object[] choicesSomeAction() {
                return null;
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction", new Class[] { int.class, Long.class });
        final Method choicesMethod = findMethod(Customer.class, "choicesSomeAction", new Class[] {  });
        reflector.setLoadSpecificationStringReturn(voidSpec);

        facetFactory.process(new ProcessMethodContext(Customer.class, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(ActionChoicesFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof ActionChoicesFacetViaMethod);
        final ActionChoicesFacetViaMethod actionChoicesFacetViaMethod = (ActionChoicesFacetViaMethod) facet;
        assertEquals(choicesMethod, actionChoicesFacetViaMethod.getMethods().get(0));

        assertTrue(methodRemover.getRemoveMethodMethodCalls().contains(choicesMethod));
    }

    public void testInstallsParameterChoicesMethodSomeArgsIsIgnored() {
        ActionChoicesFacetFactory facetFactory = new ActionChoicesFacetFactory();
        facetFactory.setSpecificationLookup(reflector);
        reflector.setLoadSpecificationStringReturn(voidSpec);

        class Customer {
            @SuppressWarnings("unused")
            public void someAction(final int x, final Long y) {}

            @SuppressWarnings("unused")
            public Object[] choicesSomeAction(final int x, final Long y) {
                return null;
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction", new Class[] { int.class, Long.class });

        facetFactory.process(new ProcessMethodContext(Customer.class, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(ActionChoicesFacet.class);
        assertNull(facet);
    }

    public static class CustomerStatic {
        public void someAction(final int x, final Long y) {}

        public static String nameSomeAction() {
            return "Another Name";
        }

        public static String descriptionSomeAction() {
            return "Some old description";
        }

        public static boolean alwaysHideSomeAction() {
            return true;
        }

        public static boolean protectSomeAction() {
            return true;
        }

        public static boolean hideSomeAction(final UserMemento userMemento) {
            return true;
        }

        public static String disableSomeAction(final UserMemento userMemento) {
            return "disabled for this user";
        }

        public static void otherAction(final int x, final Long y) {}

        public static boolean alwaysHideOtherAction() {
            return false;
        }

        public static boolean protectOtherAction() {
            return false;
        }
    }

    public void testInstallsNamedFacetUsingNameMethodAndRemovesMethod() {
        NamedFacetViaNameMethodFacetFactory facetFactory = new NamedFacetViaNameMethodFacetFactory();
        facetFactory.setSpecificationLookup(reflector);
        reflector.setLoadSpecificationStringReturn(voidSpec);

        final Method actionMethod = findMethod(CustomerStatic.class, "someAction", new Class[] { int.class, Long.class });
        final Method nameMethod = findMethod(CustomerStatic.class, "nameSomeAction");

        facetFactory.process(new ProcessMethodContext(CustomerStatic.class, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(NamedFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof NamedFacetAbstract);
        final NamedFacetAbstract namedFacetAbstract = (NamedFacetAbstract) facet;
        assertEquals("Another Name", namedFacetAbstract.value());

        assertTrue(methodRemover.getRemoveMethodMethodCalls().contains(nameMethod));
    }

    public void testInstallsDescribedAsFacetUsingDescriptionAndRemovesMethod() {
        DescribedAsFacetViaDescriptionMethodFacetFactory facetFactory = new DescribedAsFacetViaDescriptionMethodFacetFactory();
        facetFactory.setSpecificationLookup(reflector);
        reflector.setLoadSpecificationStringReturn(voidSpec);

        final Method actionMethod = findMethod(CustomerStatic.class, "someAction", new Class[] { int.class, Long.class });
        final Method descriptionMethod = findMethod(CustomerStatic.class, "descriptionSomeAction");

        facetFactory.process(new ProcessMethodContext(CustomerStatic.class, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(DescribedAsFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof DescribedAsFacetAbstract);
        final DescribedAsFacetAbstract describedAsFacetAbstract = (DescribedAsFacetAbstract) facet;
        assertEquals("Some old description", describedAsFacetAbstract.value());

        assertTrue(methodRemover.getRemoveMethodMethodCalls().contains(descriptionMethod));
    }

    public void testInstallsHiddenFacetUsingAlwaysHideAndRemovesMethod() {
        HiddenFacetViaAlwaysHideMethodFacetFactory facetFactory = new HiddenFacetViaAlwaysHideMethodFacetFactory();
        facetFactory.setSpecificationLookup(reflector);

        final Method actionMethod = findMethod(CustomerStatic.class, "someAction", new Class[] { int.class, Long.class });
        final Method alwaysHideMethod = findMethod(CustomerStatic.class, "alwaysHideSomeAction", new Class[] { });
        reflector.setLoadSpecificationStringReturn(voidSpec);

        facetFactory.process(new ProcessMethodContext(CustomerStatic.class, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(HiddenFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof HiddenFacetAbstract);
        final HiddenFacetAbstract hiddenFacetAbstract = (HiddenFacetAbstract) facet;
        assertEquals(When.ALWAYS, hiddenFacetAbstract.value());

        assertTrue(methodRemover.getRemoveMethodMethodCalls().contains(alwaysHideMethod));
    }

    public void testInstallsHiddenFacetUsingAlwaysHideWhenNotAndRemovesMethod() {
        HiddenFacetViaAlwaysHideMethodFacetFactory facetFactory = new HiddenFacetViaAlwaysHideMethodFacetFactory();
        facetFactory.setSpecificationLookup(reflector);
        reflector.setLoadSpecificationStringReturn(voidSpec);

        final Method actionMethod = findMethod(CustomerStatic.class, "otherAction", new Class[] { int.class, Long.class });
        final Method alwaysHideMethod = findMethod(CustomerStatic.class, "alwaysHideOtherAction", new Class[] { });

        facetFactory.process(new ProcessMethodContext(CustomerStatic.class, actionMethod, methodRemover, facetedMethod));

        assertNull(facetedMethod.getFacet(HiddenFacet.class));

        assertTrue(methodRemover.getRemoveMethodMethodCalls().contains(alwaysHideMethod));
    }

    public void testInstallsDisabledFacetUsingProtectAndRemovesMethod() {
        DisabledFacetViaProtectMethodFacetFactory facetFactory = new DisabledFacetViaProtectMethodFacetFactory();
        facetFactory.setSpecificationLookup(reflector);
        reflector.setLoadSpecificationStringReturn(voidSpec);

        final Method actionMethod = findMethod(CustomerStatic.class, "someAction", new Class[] { int.class, Long.class });
        final Method protectMethod = findMethod(CustomerStatic.class, "protectSomeAction", new Class[] { });

        facetFactory.process(new ProcessMethodContext(CustomerStatic.class, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(DisabledFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof DisabledFacetAbstract);
        final DisabledFacetAbstract disabledFacetAbstract = (DisabledFacetAbstract) facet;
        assertEquals(When.ALWAYS, disabledFacetAbstract.value());

        assertTrue(methodRemover.getRemoveMethodMethodCalls().contains(protectMethod));
    }

    public void testDoesNotInstallDisabledFacetUsingProtectWhenNotAndRemovesMethod() {
        DisabledFacetViaProtectMethodFacetFactory facetFactory = new DisabledFacetViaProtectMethodFacetFactory();
        facetFactory.setSpecificationLookup(reflector);
        reflector.setLoadSpecificationStringReturn(voidSpec);

        final Method actionMethod = findMethod(CustomerStatic.class, "otherAction", new Class[] { int.class, Long.class });
        final Method protectMethod = findMethod(CustomerStatic.class, "protectOtherAction", new Class[] { });

        facetFactory.process(new ProcessMethodContext(CustomerStatic.class, actionMethod, methodRemover, facetedMethod));

        assertNull(facetedMethod.getFacet(DisabledFacet.class));

        assertTrue(methodRemover.getRemoveMethodMethodCalls().contains(protectMethod));
    }

    public void testInstallsHiddenForSessionFacetAndRemovesMethod() {
        
        HiddenFacetViaHideForSessionMethodFacetFactory facetFactory = new HiddenFacetViaHideForSessionMethodFacetFactory();
        facetFactory.setSpecificationLookup(reflector);
        reflector.setLoadSpecificationStringReturn(voidSpec);
        
        final Method actionMethod = findMethod(CustomerStatic.class, "someAction", new Class[] { int.class, Long.class });
        final Method hideMethod = findMethod(CustomerStatic.class, "hideSomeAction", new Class[] { UserMemento.class });

        facetFactory.process(new ProcessMethodContext(CustomerStatic.class, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(HideForSessionFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof HideForSessionFacetViaMethod);
        final HideForSessionFacetViaMethod hideForSessionFacetViaMethod = (HideForSessionFacetViaMethod) facet;
        assertEquals(hideMethod, hideForSessionFacetViaMethod.getMethods().get(0));

        assertTrue(methodRemover.getRemoveMethodMethodCalls().contains(hideMethod));
    }

    public void testInstallsDisabledForSessionFacetAndRemovesMethod() {
        DisabledFacetViaDisableForSessionMethodFacetFactory facetFactory = new DisabledFacetViaDisableForSessionMethodFacetFactory();
        facetFactory.setSpecificationLookup(reflector);
        reflector.setLoadSpecificationStringReturn(voidSpec);

        final Method actionMethod = findMethod(CustomerStatic.class, "someAction", new Class[] { int.class, Long.class });
        final Method disableMethod = findMethod(CustomerStatic.class, "disableSomeAction", new Class[] { UserMemento.class });

        facetFactory.process(new ProcessMethodContext(CustomerStatic.class, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(DisableForSessionFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof DisableForSessionFacetViaMethod);
        final DisableForSessionFacetViaMethod disableForSessionFacetViaMethod = (DisableForSessionFacetViaMethod) facet;
        assertEquals(disableMethod, disableForSessionFacetViaMethod.getMethods().get(0));

        assertTrue(methodRemover.getRemoveMethodMethodCalls().contains(disableMethod));
    }

    public void testActionReturnTypeWhenVoid() {
        ActionInvocationFacetFactory facetFactory = new ActionInvocationFacetFactory();
        facetFactory.setSpecificationLookup(reflector);
        reflector.setLoadSpecificationStringReturn(voidSpec);

        class Customer {
            @SuppressWarnings("unused")
            public void someAction() {}
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.process(new ProcessMethodContext(Customer.class, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(ActionInvocationFacet.class);
        final ActionInvocationFacetViaMethod actionInvocationFacetViaMethod = (ActionInvocationFacetViaMethod) facet;
        assertEquals(voidSpec, actionInvocationFacetViaMethod.getReturnType());
    }

    public void testActionReturnTypeWhenNotVoid() {
        ActionInvocationFacetFactory facetFactory = new ActionInvocationFacetFactory();
        facetFactory.setSpecificationLookup(reflector);
        reflector.setLoadSpecificationStringReturn(stringSpec);

        class Customer {
            @SuppressWarnings("unused")
            public String someAction() {
                return null;
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.process(new ProcessMethodContext(Customer.class, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(ActionInvocationFacet.class);
        final ActionInvocationFacetViaMethod actionInvocationFacetViaMethod = (ActionInvocationFacetViaMethod) facet;
        assertEquals(stringSpec, actionInvocationFacetViaMethod.getReturnType());
    }

    public void testActionOnType() {
        ActionInvocationFacetFactory facetFactory = new ActionInvocationFacetFactory();
        facetFactory.setSpecificationLookup(reflector);
        reflector.setLoadSpecificationStringReturn(customerSpec);

        class Customer {
            @SuppressWarnings("unused")
            public String someAction() {
                return null;
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.process(new ProcessMethodContext(Customer.class, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(ActionInvocationFacet.class);
        final ActionInvocationFacetViaMethod actionInvocationFacetViaMethod = (ActionInvocationFacetViaMethod) facet;
        assertEquals(customerSpec, actionInvocationFacetViaMethod.getOnType());
    }

    public void testInstallsParameterDefaultsMethodAndRemovesMethod() {
        ActionParameterDefaultsFacetFactory facetFactory = new ActionParameterDefaultsFacetFactory();
        facetFactory.setSpecificationLookup(reflector);
        reflector.setLoadSpecificationStringReturn(voidSpec);

        class Customer {
            @SuppressWarnings("unused")
            public void someAction(final int x, final long y) {}

            @SuppressWarnings("unused")
            public int default0SomeAction() {
                return 0;
            }

            @SuppressWarnings("unused")
            public long default1SomeAction() {
                return 0;
            }
        }

        Method actionMethod = findMethod(Customer.class, "someAction", new Class[] { int.class, long.class });
        Method default0Method = findMethod(Customer.class, "default0SomeAction", new Class[] {});
        Method default1Method = findMethod(Customer.class, "default1SomeAction", new Class[] {});

        FacetedMethod facetHolderWithParms = FacetedMethod.createActionFacetedMethod(Customer.class, actionMethod);

        facetFactory.process(new ProcessMethodContext(Customer.class, actionMethod, methodRemover, facetHolderWithParms));

        final Facet facet0 = facetHolderWithParms.getParameters().get(0).getFacet(ActionParameterDefaultsFacet.class);
        assertNotNull(facet0);
        assertTrue(facet0 instanceof ActionParameterDefaultsFacetViaMethod);
        final ActionParameterDefaultsFacetViaMethod actionDefaultFacetViaMethod0 = (ActionParameterDefaultsFacetViaMethod) facet0;
        assertEquals(default0Method, actionDefaultFacetViaMethod0.getMethods().get(0));

        assertTrue(methodRemover.getRemoveMethodMethodCalls().contains(default0Method));

        final Facet facet1 = facetHolderWithParms.getParameters().get(1).getFacet(ActionParameterDefaultsFacet.class);
        assertNotNull(facet1);
        assertTrue(facet1 instanceof ActionParameterDefaultsFacetViaMethod);
        final ActionParameterDefaultsFacetViaMethod actionDefaultFacetViaMethod1 = (ActionParameterDefaultsFacetViaMethod) facet1;
        assertEquals(default1Method, actionDefaultFacetViaMethod1.getMethods().get(0));

        assertTrue(methodRemover.getRemoveMethodMethodCalls().contains(default1Method));

    }

    public void testInstallsParameterChoicesMethodAndRemovesMethod() {
        ActionParameterChoicesFacetFactory facetFactory = new ActionParameterChoicesFacetFactory();
        facetFactory.setSpecificationLookup(reflector);
        reflector.setLoadSpecificationStringReturn(voidSpec);

        class Customer {
            @SuppressWarnings("unused")
			public void someAction(final int x, final long y) {}

            @SuppressWarnings("unused")
			public List<Integer> choices0SomeAction() {
                return Collections.emptyList();
            }

            @SuppressWarnings("unused")
			public List<Long> choices1SomeAction() {
                return Collections.emptyList();
            }
        }

        Method actionMethod = findMethod(Customer.class, "someAction", new Class[] { int.class, long.class });
        Method choices0Method = findMethod(Customer.class, "choices0SomeAction", new Class[] {});
        Method choices1Method = findMethod(Customer.class, "choices1SomeAction", new Class[] {});

        FacetedMethod facetHolderWithParms = FacetedMethod.createActionFacetedMethod(Customer.class, actionMethod);

        facetFactory.process(new ProcessMethodContext(Customer.class, actionMethod, methodRemover, facetHolderWithParms));

        final Facet facet0 = facetHolderWithParms.getParameters().get(0).getFacet(ActionParameterChoicesFacet.class);
        assertNotNull(facet0);
        assertTrue(facet0 instanceof ActionParameterChoicesFacetViaMethod);
        final ActionParameterChoicesFacetViaMethod actionChoicesFacetViaMethod0 = (ActionParameterChoicesFacetViaMethod) facet0;
        assertEquals(choices0Method, actionChoicesFacetViaMethod0.getMethods().get(0));

        assertTrue(methodRemover.getRemoveMethodMethodCalls().contains(choices0Method));

        final Facet facet1 = facetHolderWithParms.getParameters().get(1).getFacet(ActionParameterChoicesFacet.class);
        assertNotNull(facet1);
        assertTrue(facet1 instanceof ActionParameterChoicesFacetViaMethod);
        final ActionParameterChoicesFacetViaMethod actionChoicesFacetViaMethod1 = (ActionParameterChoicesFacetViaMethod) facet1;
        assertEquals(choices1Method, actionChoicesFacetViaMethod1.getMethods().get(0));

        assertTrue(methodRemover.getRemoveMethodMethodCalls().contains(choices1Method));

    }


    public void testActionsPickedUpFromSuperclass() {
        ActionInvocationFacetFactory facetFactory = new ActionInvocationFacetFactory();
        facetFactory.setSpecificationLookup(reflector);
        reflector.setLoadSpecificationStringReturn(voidSpec);

        class Customer {
            @SuppressWarnings("unused")
            public void someAction(final int x, final long y) {}
        }

        class CustomerEx extends Customer {
        }

        Method actionMethod = findMethod(CustomerEx.class, "someAction", new Class[] { int.class, long.class });


        FacetedMethod facetHolderWithParms = FacetedMethod.createActionFacetedMethod(CustomerEx.class, actionMethod);


        facetFactory.process(new ProcessMethodContext(CustomerEx.class, actionMethod, methodRemover, facetHolderWithParms));

        final Facet facet0 = facetHolderWithParms.getFacet(ActionInvocationFacet.class);
        assertNotNull(facet0);
    }

    public void testActionsPickedUpFromSuperclassButHelpersFromSubClass() {
        ActionInvocationFacetFactory facetFactory = new ActionInvocationFacetFactory();
        facetFactory.setSpecificationLookup(reflector);
        reflector.setLoadSpecificationStringReturn(voidSpec);

        ActionParameterChoicesFacetFactory facetFactoryForChoices = new ActionParameterChoicesFacetFactory();
        facetFactoryForChoices.setSpecificationLookup(reflector);
        reflector.setLoadSpecificationStringReturn(voidSpec);

        DisabledFacetViaDisableMethodFacetFactory facetFactoryForDisable = new DisabledFacetViaDisableMethodFacetFactory();
        facetFactoryForDisable.setSpecificationLookup(reflector);
        reflector.setLoadSpecificationStringReturn(voidSpec);

        class Customer {
            @SuppressWarnings("unused")
            public void someAction(final int x, final long y) {}

            @SuppressWarnings("unused")
            public int[] choices0SomeAction() {
                return new int[0];
            }
        }

        class CustomerEx extends Customer {
            @Override
            public int[] choices0SomeAction() {
                return new int[0];
            }

            @SuppressWarnings("unused")
            public long[] choices1SomeAction() {
                return new long[0];
            }

            @SuppressWarnings("unused")
            public String disableSomeAction() {
                return null;
            }
        }

        Method actionMethod = findMethod(CustomerEx.class, "someAction", new Class[] { int.class, long.class });
        Method choices0Method = findMethod(CustomerEx.class, "choices0SomeAction", new Class[] {});
        Method choices1Method = findMethod(CustomerEx.class, "choices1SomeAction", new Class[] {});
        Method disableMethod = findMethod(CustomerEx.class, "disableSomeAction", new Class[] {});

        FacetedMethod facetHolderWithParms = FacetedMethod.createActionFacetedMethod(CustomerEx.class, actionMethod);

        final ProcessMethodContext processMethodContext = new ProcessMethodContext(CustomerEx.class, actionMethod, methodRemover, facetHolderWithParms);
        facetFactory.process(processMethodContext);
        facetFactoryForChoices.process(processMethodContext);
        facetFactoryForDisable.process(processMethodContext);

        final Facet facet0 = facetHolderWithParms.getFacet(ActionInvocationFacet.class);
        assertNotNull(facet0);

        final Facet facet1 = facetHolderWithParms.getParameters().get(0).getFacet(ActionParameterChoicesFacet.class);
        assertNotNull(facet1);
        assertTrue(facet1 instanceof ActionParameterChoicesFacetViaMethod);
        final ActionParameterChoicesFacetViaMethod actionChoicesFacetViaMethod0 = (ActionParameterChoicesFacetViaMethod) facet1;
        assertEquals(choices0Method, actionChoicesFacetViaMethod0.getMethods().get(0));

        final Facet facet2 = facetHolderWithParms.getParameters().get(1).getFacet(ActionParameterChoicesFacet.class);
        assertNotNull(facet2);
        assertTrue(facet2 instanceof ActionParameterChoicesFacetViaMethod);
        final ActionParameterChoicesFacetViaMethod actionChoicesFacetViaMethod1 = (ActionParameterChoicesFacetViaMethod) facet2;
        assertEquals(choices1Method, actionChoicesFacetViaMethod1.getMethods().get(0));

        final Facet facet3 = facetHolderWithParms.getFacet(DisableForContextFacet.class);
        assertNotNull(facet3);
        assertTrue(facet3 instanceof DisableForContextFacetViaMethod);
        final DisableForContextFacetViaMethod disableFacetViaMethod = (DisableForContextFacetViaMethod) facet3;
        assertEquals(disableMethod, disableFacetViaMethod.getMethods().get(0));

    }


    public void testBothChoicesMethodCausesException() {

        ActionChoicesFacetFactory facetFactory = new ActionChoicesFacetFactory();
        facetFactory.setSpecificationLookup(reflector);
        reflector.setLoadSpecificationStringReturn(voidSpec);

        ActionParameterChoicesFacetFactory facetFactoryForParams = new ActionParameterChoicesFacetFactory();
        facetFactoryForParams.setSpecificationLookup(reflector);
        reflector.setLoadSpecificationStringReturn(voidSpec);

        class Customer {
            @SuppressWarnings("unused")
            public void someAction(final int x, final long y) {}

            @SuppressWarnings("unused")
            public int[] choices0SomeAction() {
                return new int[0];
            }

            @SuppressWarnings("unused")
            public long[] choices1SomeAction() {
                return new long[0];
            }

            @SuppressWarnings("unused")
            public Object[][] choicesSomeAction() {
                return null;
            }
        }

        Method actionMethod = findMethod(Customer.class, "someAction", new Class[] { int.class, long.class });
        FacetedMethod facetHolderWithParms = FacetedMethod.createActionFacetedMethod(Customer.class, actionMethod);

        final ProcessMethodContext processMethodContext = new ProcessMethodContext(Customer.class, actionMethod, methodRemover, facetHolderWithParms);
        facetFactory.process(processMethodContext);
        try {
            facetFactoryForParams.process(processMethodContext);
            fail("exception expected");
        } catch (org.apache.isis.core.metamodel.exceptions.MetaModelException expected) {
            // ignore
        }
    }

    public void testBothDefaultMethodCausesException() {
        ActionDefaultsFacetFactory facetFactory = new ActionDefaultsFacetFactory();
        facetFactory.setSpecificationLookup(reflector);
        reflector.setLoadSpecificationStringReturn(voidSpec);

        ActionParameterDefaultsFacetFactory facetFactoryForParams = new ActionParameterDefaultsFacetFactory();
        facetFactoryForParams.setSpecificationLookup(reflector);
        reflector.setLoadSpecificationStringReturn(voidSpec);

        class Customer {
            @SuppressWarnings("unused")
            public void someAction(final int x, final long y) {}

            @SuppressWarnings("unused")
            public int default0SomeAction() {
                return 0;
            }

            @SuppressWarnings("unused")
            public long default1SomeAction() {
                return 0;
            }

            @SuppressWarnings("unused")
            public Object[] defaultSomeAction() {
                return null;
            }
        }

        Method actionMethod = findMethod(Customer.class, "someAction", new Class[] { int.class, long.class });
        FacetedMethod facetHolderWithParms = FacetedMethod.createActionFacetedMethod(Customer.class, actionMethod);

        final ProcessMethodContext processMethodContext = new ProcessMethodContext(Customer.class, actionMethod, methodRemover, facetHolderWithParms);
        facetFactory.process(processMethodContext);
        try {
            facetFactoryForParams.process(processMethodContext);
            fail("exception expected");
        } catch (org.apache.isis.core.metamodel.exceptions.MetaModelException expected) {

        }
    }

}

