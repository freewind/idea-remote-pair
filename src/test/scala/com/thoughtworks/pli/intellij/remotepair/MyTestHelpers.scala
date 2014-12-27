package com.thoughtworks.pli.intellij.remotepair

import org.mockito.{Mockito => JMockito}
import org.specs2.matcher.ThrownExpectations
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

import scala.reflect.ClassTag

trait MyMockito extends Mockito {
  def deepMock[T: ClassTag]: T = mock[T](JMockito.withSettings.defaultAnswer(JMockito.RETURNS_DEEP_STUBS))
  def reset[T](mocks: T*) = JMockito.reset(mocks)
}

trait MySpecification extends Specification with MyMockito with ThrownExpectations

trait MyMocking extends Scope with MyMockito with ThrownExpectations

