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
package org.sonar.cxx.sslr.internal.ast;

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.AstNodeType;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Predicate;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.sonar.cxx.sslr.internal.ast.select.AstSelectFactory;
import org.sonar.cxx.sslr.internal.ast.select.ListAstSelect;
import org.sonar.cxx.sslr.internal.ast.select.SingleAstSelect;

public class ListAstSelectTest {

  private AstNode node1, node2;
  private ListAstSelect select;

  @BeforeEach
  public void init() {
    node1 = mock(AstNode.class);
    node2 = mock(AstNode.class);
    select = new ListAstSelect(Arrays.asList(node1, node2));
  }

  @Test
  public void test_children_when_no_children() {
    assertThat((Object) select.children()).isSameAs(AstSelectFactory.empty());
    assertThat((Object) select.children(mock(AstNodeType.class))).isSameAs(AstSelectFactory.empty());
    assertThat((Object) select.children(mock(AstNodeType.class), mock(AstNodeType.class))).isSameAs(AstSelectFactory
      .empty());
  }

  @Test
  public void test_children_when_one_child() {
    var type1 = mock(AstNodeType.class);
    var type2 = mock(AstNodeType.class);
    var child = mock(AstNode.class);
    when(node1.getChildren()).thenReturn(Collections.singletonList(child));

    when(node1.getFirstChild()).thenReturn(child);
    var children = select.children();
    assertThat((Object) children).isInstanceOf(SingleAstSelect.class);
    assertThat(children).containsOnly(child);

    when(node1.getChildren()).thenReturn(Collections.singletonList(child));

    children = select.children(type1);
    assertThat((Object) children).isSameAs(AstSelectFactory.empty());

    when(child.getType()).thenReturn(type1);
    children = select.children(type1);
    assertThat((Object) children).isInstanceOf(SingleAstSelect.class);
    assertThat(children).containsOnly(child);

    children = select.children(type1, type2);
    assertThat((Object) children).isSameAs(AstSelectFactory.empty());

    when(child.is(type1, type2)).thenReturn(true);
    children = select.children(type1, type2);
    assertThat((Object) children).isInstanceOf(SingleAstSelect.class);
    assertThat(children).containsOnly(child);
  }

  @Test
  public void test_chilren_when_more_than_one_child() {
    var type1 = mock(AstNodeType.class);
    var type2 = mock(AstNodeType.class);
    var child1 = mock(AstNode.class);
    var child2 = mock(AstNode.class);
    when(node1.getChildren()).thenReturn(Collections.singletonList(child1));
    when(node2.getChildren()).thenReturn(Collections.singletonList(child2));

    var children = select.children();
    assertThat((Object) children).isInstanceOf(ListAstSelect.class);
    assertThat(children).containsOnly(child1, child2);

    children = select.children(type1);
    assertThat((Object) children).isSameAs(AstSelectFactory.empty());

    when(child1.getType()).thenReturn(type1);
    children = select.children(type1);
    assertThat((Object) children).isInstanceOf(SingleAstSelect.class);
    assertThat(children).containsOnly(child1);

    when(child2.getType()).thenReturn(type1);
    children = select.children(type1);
    assertThat((Object) children).isInstanceOf(ListAstSelect.class);
    assertThat(children).containsOnly(child1, child2);

    children = select.children(type1, type2);
    assertThat((Object) children).isSameAs(AstSelectFactory.empty());

    when(child1.is(type1, type2)).thenReturn(true);
    children = select.children(type1, type2);
    assertThat((Object) children).isInstanceOf(SingleAstSelect.class);
    assertThat(children).containsOnly(child1);

    when(child2.is(type1, type2)).thenReturn(true);
    children = select.children(type1, type2);
    assertThat((Object) children).isInstanceOf(ListAstSelect.class);
    assertThat(children).containsOnly(child1, child2);
  }

  @Test
  public void test_nextSibling() {
    assertThat((Object) select.nextSibling()).isSameAs(AstSelectFactory.empty());

    var sibling1 = mock(AstNode.class);
    when(node1.getNextSibling()).thenReturn(sibling1);
    assertThat((Object) select.nextSibling()).isInstanceOf(SingleAstSelect.class);
    assertThat(select.nextSibling()).containsOnly(sibling1);

    var sibling2 = mock(AstNode.class);
    when(node2.getNextSibling()).thenReturn(sibling2);
    assertThat((Object) select.nextSibling()).isInstanceOf(ListAstSelect.class);
    assertThat(select.nextSibling()).containsOnly(sibling1, sibling2);
  }

  @Test
  public void test_previousSibling() {
    assertThat((Object) select.previousSibling()).isSameAs(AstSelectFactory.empty());

    var sibling1 = mock(AstNode.class);
    when(node1.getPreviousSibling()).thenReturn(sibling1);
    assertThat((Object) select.previousSibling()).isInstanceOf(SingleAstSelect.class);
    assertThat(select.previousSibling()).containsOnly(sibling1);

    var sibling2 = mock(AstNode.class);
    when(node2.getPreviousSibling()).thenReturn(sibling2);
    assertThat((Object) select.previousSibling()).isInstanceOf(ListAstSelect.class);
    assertThat(select.previousSibling()).containsOnly(sibling1, sibling2);
  }

  @Test
  public void test_parent() {
    assertThat((Object) select.parent()).isSameAs(AstSelectFactory.empty());

    var parent1 = mock(AstNode.class);
    when(node1.getParent()).thenReturn(parent1);
    assertThat((Object) select.parent()).isInstanceOf(SingleAstSelect.class);
    assertThat(select.parent()).containsOnly(parent1);

    var parent2 = mock(AstNode.class);
    when(node2.getParent()).thenReturn(parent2);
    assertThat((Object) select.parent()).isInstanceOf(ListAstSelect.class);
    assertThat(select.parent()).containsOnly(parent1, parent2);
  }

  @Test
  public void test_firstAncestor_by_type() {
    var type = mock(AstNodeType.class);
    assertThat((Object) select.firstAncestor(type)).isSameAs(AstSelectFactory.empty());

    var parent = mock(AstNode.class);
    when(node1.getParent()).thenReturn(parent);
    var ancestor1 = mock(AstNode.class);
    when(ancestor1.getType()).thenReturn(type);
    when(parent.getParent()).thenReturn(ancestor1);
    assertThat((Object) select.firstAncestor(type)).isInstanceOf(SingleAstSelect.class);
    assertThat(select.firstAncestor(type)).containsOnly(ancestor1);

    var ancestor2 = mock(AstNode.class);
    when(ancestor2.getType()).thenReturn(type);
    when(node2.getParent()).thenReturn(ancestor2);
    assertThat((Object) select.firstAncestor(type)).isInstanceOf(ListAstSelect.class);
    assertThat(select.firstAncestor(type)).containsOnly(ancestor1, ancestor2);
  }

  @Test
  public void test_firstAncestor_by_types() {
    var type1 = mock(AstNodeType.class);
    var type2 = mock(AstNodeType.class);
    assertThat((Object) select.firstAncestor(type1, type2)).isSameAs(AstSelectFactory.empty());

    var parent = mock(AstNode.class);
    when(node1.getParent()).thenReturn(parent);
    var ancestor1 = mock(AstNode.class);
    when(ancestor1.is(type1, type2)).thenReturn(true);
    when(parent.getParent()).thenReturn(ancestor1);
    assertThat((Object) select.firstAncestor(type1, type2)).isInstanceOf(SingleAstSelect.class);
    assertThat(select.firstAncestor(type1, type2)).containsOnly(ancestor1);

    var ancestor2 = mock(AstNode.class);
    when(ancestor2.is(type1, type2)).thenReturn(true);
    when(node2.getParent()).thenReturn(ancestor2);
    assertThat((Object) select.firstAncestor(type1, type2)).isInstanceOf(ListAstSelect.class);
    assertThat(select.firstAncestor(type1, type2)).containsOnly(ancestor1, ancestor2);
  }

  @Test
  public void test_descendants() {
    assertThat((Object) select.descendants(mock(AstNodeType.class))).isSameAs(AstSelectFactory.empty());
    assertThat((Object) select.descendants(mock(AstNodeType.class), mock(AstNodeType.class))).isSameAs(AstSelectFactory
      .empty());
  }

  @Test
  public void test_isEmpty() {
    assertThat(select.isEmpty()).isFalse();
  }

  @Test
  public void test_isNotEmpty() {
    assertThat(select.isNotEmpty()).isTrue();
  }

  @Test
  public void test_filter_by_type() {
    var type = mock(AstNodeType.class);
    assertThat((Object) select.filter(type)).isSameAs(AstSelectFactory.empty());

    when(node1.getType()).thenReturn(type);
    assertThat((Object) select.filter(type)).isInstanceOf(SingleAstSelect.class);
    assertThat(select.filter(type)).containsOnly(node1);

    when(node2.getType()).thenReturn(type);
    assertThat((Object) select.filter(type)).isInstanceOf(ListAstSelect.class);
    assertThat(select.filter(type)).containsOnly(node1, node2);
  }

  @Test
  public void test_filter_by_types() {
    var type1 = mock(AstNodeType.class);
    var type2 = mock(AstNodeType.class);
    assertThat((Object) select.filter(type1, type2)).isSameAs(AstSelectFactory.empty());

    when(node1.is(type1, type2)).thenReturn(true);
    assertThat((Object) select.filter(type1, type2)).isInstanceOf(SingleAstSelect.class);
    assertThat(select.filter(type1, type2)).containsOnly(node1);

    when(node2.is(type1, type2)).thenReturn(true);
    assertThat((Object) select.filter(type1, type2)).isInstanceOf(ListAstSelect.class);
    assertThat(select.filter(type1, type2)).containsOnly(node1, node2);
  }

  @Test
  public void test_filter() {
    Predicate<AstNode> predicate = mock(Predicate.class);
    assertThat((Object) select.filter(predicate)).isSameAs(AstSelectFactory.empty());

    when(predicate.test(node1)).thenReturn(true);
    assertThat((Object) select.filter(predicate)).isInstanceOf(SingleAstSelect.class);
    assertThat(select.filter(predicate)).containsOnly(node1);

    when(predicate.test(node2)).thenReturn(true);
    assertThat((Object) select.filter(predicate)).isInstanceOf(ListAstSelect.class);
    assertThat(select.filter(predicate)).containsOnly(node1, node2);
  }

  @Test
  public void test_get() {
    assertThat(select.get(0)).isSameAs(node1);
    assertThat(select.get(1)).isSameAs(node2);
  }

  @Test
  public void test_get_non_existing() {
    var thrown = catchThrowableOfType(
      () -> select.get(2),
      IndexOutOfBoundsException.class);
    assertThat(thrown).isInstanceOf(IndexOutOfBoundsException.class);
  }

  @Test
  public void test_size() {
    assertThat(select.size()).isEqualTo(2);
  }

  @Test
  public void test_iterator() {
    assertThat(select.iterator()).toIterable().containsOnly(node1, node2);
  }

}