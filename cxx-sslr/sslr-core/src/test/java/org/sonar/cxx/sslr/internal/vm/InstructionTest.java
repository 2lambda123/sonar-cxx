/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2022 SonarOpenCommunity
 * http://github.com/SonarOpenCommunity/sonar-cxx
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
/**
 * fork of SonarSource Language Recognizer: https://github.com/SonarSource/sslr
 * Copyright (C) 2010-2021 SonarSource SA / mailto:info AT sonarsource DOT com / license: LGPL v3
 */
package org.sonar.cxx.sslr.internal.vm;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.sonar.cxx.sslr.grammar.GrammarException;
import org.sonar.cxx.sslr.internal.matchers.Matcher;
import org.sonar.cxx.sslr.internal.vm.Instruction.BackCommitInstruction;
import org.sonar.cxx.sslr.internal.vm.Instruction.BacktrackInstruction;
import org.sonar.cxx.sslr.internal.vm.Instruction.CallInstruction;
import org.sonar.cxx.sslr.internal.vm.Instruction.ChoiceInstruction;
import org.sonar.cxx.sslr.internal.vm.Instruction.CommitInstruction;
import org.sonar.cxx.sslr.internal.vm.Instruction.CommitVerifyInstruction;
import org.sonar.cxx.sslr.internal.vm.Instruction.EndInstruction;
import org.sonar.cxx.sslr.internal.vm.Instruction.FailTwiceInstruction;
import org.sonar.cxx.sslr.internal.vm.Instruction.IgnoreErrorsInstruction;
import org.sonar.cxx.sslr.internal.vm.Instruction.JumpInstruction;
import org.sonar.cxx.sslr.internal.vm.Instruction.PredicateChoiceInstruction;
import org.sonar.cxx.sslr.internal.vm.Instruction.RetInstruction;

public class InstructionTest {

  private final Machine machine = mock(Machine.class);

  @Test
  public void jump() {
    var instruction = Instruction.jump(42);
    assertThat(instruction).isInstanceOf(JumpInstruction.class);
    assertThat(instruction.toString()).isEqualTo("Jump 42");
    assertThat(instruction.equals(Instruction.jump(42))).isTrue();
    assertThat(instruction.equals(Instruction.jump(13))).isFalse();
    assertThat(instruction.equals(new Object())).isFalse();
    assertThat(instruction.hashCode()).isEqualTo(42);

    instruction.execute(machine);
    var inOrder = Mockito.inOrder(machine);
    inOrder.verify(machine).jump(42);
    verifyNoMoreInteractions(machine);
  }

  @Test
  public void call() {
    var matcher = mock(Matcher.class);
    var instruction = Instruction.call(42, matcher);
    assertThat(instruction).isInstanceOf(CallInstruction.class);
    assertThat(instruction.toString()).isEqualTo("Call 42");
    assertThat(instruction.equals(Instruction.call(42, matcher))).isTrue();
    assertThat(instruction.equals(Instruction.call(42, mock(Matcher.class)))).isFalse();
    assertThat(instruction.equals(Instruction.call(13, matcher))).isFalse();
    assertThat(instruction.equals(new Object())).isFalse();
    assertThat(instruction.hashCode()).isEqualTo(42);

    instruction.execute(machine);
    var inOrder = Mockito.inOrder(machine);
    inOrder.verify(machine).pushReturn(1, matcher, 42);
    verifyNoMoreInteractions(machine);
  }

  @Test
  public void choice() {
    var instruction = Instruction.choice(42);
    assertThat(instruction).isInstanceOf(ChoiceInstruction.class);
    assertThat(instruction.toString()).isEqualTo("Choice 42");
    assertThat(instruction.equals(Instruction.choice(42))).isTrue();
    assertThat(instruction.equals(Instruction.choice(13))).isFalse();
    assertThat(instruction.equals(new Object())).isFalse();
    assertThat(instruction.hashCode()).isEqualTo(42);

    instruction.execute(machine);
    var inOrder = Mockito.inOrder(machine);
    inOrder.verify(machine).pushBacktrack(42);
    inOrder.verify(machine).jump(1);
    verifyNoMoreInteractions(machine);
  }

  @Test
  public void predicateChoice() {
    var instruction = Instruction.predicateChoice(42);
    assertThat(instruction).isInstanceOf(PredicateChoiceInstruction.class);
    assertThat(instruction.toString()).isEqualTo("PredicateChoice 42");
    assertThat(instruction.equals(Instruction.predicateChoice(42))).isTrue();
    assertThat(instruction.equals(Instruction.predicateChoice(13))).isFalse();
    assertThat(instruction.equals(new Object())).isFalse();
    assertThat(instruction.hashCode()).isEqualTo(42);

    instruction.execute(machine);
    var inOrder = Mockito.inOrder(machine);
    inOrder.verify(machine).pushBacktrack(42);
    inOrder.verify(machine).setIgnoreErrors(true);
    inOrder.verify(machine).jump(1);
    verifyNoMoreInteractions(machine);
  }

  @Test
  public void commit() {
    var instruction = Instruction.commit(42);
    assertThat(instruction).isInstanceOf(CommitInstruction.class);
    assertThat(instruction.toString()).isEqualTo("Commit " + 42);
    assertThat(instruction.equals(Instruction.commit(42))).isTrue();
    assertThat(instruction.equals(Instruction.commit(13))).isFalse();
    assertThat(instruction.equals(new Object())).isFalse();
    assertThat(instruction.hashCode()).isEqualTo(42);

    var stack = new MachineStack().getOrCreateChild();
    when(machine.peek()).thenReturn(stack);
    instruction.execute(machine);
    var inOrder = Mockito.inOrder(machine);
    inOrder.verify(machine, times(2)).peek();
    inOrder.verify(machine).pop();
    inOrder.verify(machine).jump(42);
    verifyNoMoreInteractions(machine);
  }

  @Test
  public void commitVerify() {
    var instruction = Instruction.commitVerify(42);
    assertThat(instruction).isInstanceOf(CommitVerifyInstruction.class);
    assertThat(instruction.toString()).isEqualTo("CommitVerify " + 42);
    assertThat(instruction.equals(Instruction.commitVerify(42))).isTrue();
    assertThat(instruction.equals(Instruction.commitVerify(13))).isFalse();
    assertThat(instruction.equals(new Object())).isFalse();
    assertThat(instruction.hashCode()).isEqualTo(42);

    var stack = new MachineStack().getOrCreateChild();
    when(machine.peek()).thenReturn(stack);
    when(machine.getIndex()).thenReturn(13);
    instruction.execute(machine);
    var inOrder = Mockito.inOrder(machine);
    inOrder.verify(machine).getIndex();
    inOrder.verify(machine, times(3)).peek();
    inOrder.verify(machine).pop();
    inOrder.verify(machine).jump(42);
    verifyNoMoreInteractions(machine);
  }

  @Test
  public void commitVerify_should_throw_exception() {
    var instruction = Instruction.commitVerify(42);
    var stack = new MachineStack().getOrCreateChild();
    stack.setIndex(13);
    when(machine.peek()).thenReturn(stack);
    when(machine.getIndex()).thenReturn(13);
    var thrown = catchThrowableOfType(
      () -> instruction.execute(machine),
      GrammarException.class);
    assertThat(thrown).hasMessage("The inner part of ZeroOrMore and OneOrMore must not allow empty matches");
  }

  @Test
  public void ret() {
    var instruction = Instruction.ret();
    assertThat(instruction).isInstanceOf(RetInstruction.class);
    assertThat(instruction.toString()).isEqualTo("Ret");
    assertThat(instruction).as("singleton").isSameAs(Instruction.ret());

    var stack = mock(MachineStack.class);
    when(stack.address()).thenReturn(42);
    when(stack.isIgnoreErrors()).thenReturn(true);
    when(machine.peek()).thenReturn(stack);
    instruction.execute(machine);
    var inOrder = Mockito.inOrder(machine);
    inOrder.verify(machine).createNode();
    inOrder.verify(machine).peek();
    inOrder.verify(machine).setIgnoreErrors(true);
    inOrder.verify(machine).setAddress(42);
    inOrder.verify(machine).popReturn();
    verifyNoMoreInteractions(machine);
  }

  @Test
  public void backtrack() {
    var instruction = Instruction.backtrack();
    assertThat(instruction).isInstanceOf(BacktrackInstruction.class);
    assertThat(instruction.toString()).isEqualTo("Backtrack");
    assertThat(instruction).as("singleton").isSameAs(Instruction.backtrack());

    instruction.execute(machine);
    var inOrder = Mockito.inOrder(machine);
    inOrder.verify(machine).backtrack();
    verifyNoMoreInteractions(machine);
  }

  @Test
  public void end() {
    var instruction = Instruction.end();
    assertThat(instruction).isInstanceOf(EndInstruction.class);
    assertThat(instruction.toString()).isEqualTo("End");
    assertThat(instruction).as("singleton").isSameAs(Instruction.end());

    instruction.execute(machine);
    var inOrder = Mockito.inOrder(machine);
    inOrder.verify(machine).setAddress(-1);
    verifyNoMoreInteractions(machine);
  }

  @Test
  public void failTwice() {
    var instruction = Instruction.failTwice();
    assertThat(instruction).isInstanceOf(FailTwiceInstruction.class);
    assertThat(instruction.toString()).isEqualTo("FailTwice");
    assertThat(instruction).as("singleton").isSameAs(Instruction.failTwice());

    var stack = mock(MachineStack.class);
    when(stack.index()).thenReturn(13);
    when(machine.peek()).thenReturn(stack);
    instruction.execute(machine);
    var inOrder = Mockito.inOrder(machine);
    inOrder.verify(machine).peek();
    inOrder.verify(machine).setIndex(13);
    inOrder.verify(machine).pop();
    inOrder.verify(machine).backtrack();
    verifyNoMoreInteractions(machine);
  }

  @Test
  public void backCommit() {
    var instruction = Instruction.backCommit(42);
    assertThat(instruction).isInstanceOf(BackCommitInstruction.class);
    assertThat(instruction.toString()).isEqualTo("BackCommit 42");
    assertThat(instruction.equals(Instruction.backCommit(42))).isTrue();
    assertThat(instruction.equals(Instruction.backCommit(13))).isFalse();
    assertThat(instruction.equals(new Object())).isFalse();
    assertThat(instruction.hashCode()).isEqualTo(42);

    var stack = mock(MachineStack.class);
    when(stack.index()).thenReturn(13);
    when(stack.isIgnoreErrors()).thenReturn(true);
    when(machine.peek()).thenReturn(stack);
    instruction.execute(machine);
    var inOrder = Mockito.inOrder(machine);
    inOrder.verify(machine).peek();
    inOrder.verify(machine).setIndex(13);
    inOrder.verify(machine).setIgnoreErrors(true);
    inOrder.verify(machine).pop();
    inOrder.verify(machine).jump(42);
    verifyNoMoreInteractions(machine);
  }

  @Test
  public void ignoreErrors() {
    var instruction = Instruction.ignoreErrors();
    assertThat(instruction).isInstanceOf(IgnoreErrorsInstruction.class);
    assertThat(instruction.toString()).isEqualTo("IgnoreErrors");
    assertThat(instruction).as("singleton").isSameAs(Instruction.ignoreErrors());

    instruction.execute(machine);
    verify(machine).setIgnoreErrors(true);
    verify(machine).jump(1);
    verifyNoMoreInteractions(machine);
  }

}