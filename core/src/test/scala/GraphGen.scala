//: ----------------------------------------------------------------------------
//: Copyright (C) 2015 Verizon.  All Rights Reserved.
//:
//:   Licensed under the Apache License, Version 2.0 (the "License");
//:   you may not use this file except in compliance with the License.
//:   You may obtain a copy of the License at
//:
//:       http://www.apache.org/licenses/LICENSE-2.0
//:
//:   Unless required by applicable law or agreed to in writing, software
//:   distributed under the License is distributed on an "AS IS" BASIS,
//:   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//:   See the License for the specific language governing permissions and
//:   limitations under the License.
//:
//: ----------------------------------------------------------------------------
package quiver

import org.scalacheck._
import org.scalacheck.Arbitrary._

object GraphGen {

  def graphGen[N: Arbitrary, A: Arbitrary, B: Arbitrary]: Gen[Graph[N,A,B]] = for {
    vs <- Gen.listOf(genNode[N,A])
    es <- Gen.listOf(genEdge[N,B])
  } yield safeMkGraph(vs, es)

  def genNode[N: Arbitrary, A: Arbitrary]: Gen[LNode[N,A]] = for {
    a <- arbitrary[A]
    v <- arbitrary[N]
  } yield LNode(v, a)

  def genEdge[N: Arbitrary, A: Arbitrary]: Gen[LEdge[N,A]] = for {
    x <- arbitrary[N]
    y <- arbitrary[N]
    a <- arbitrary[A]
  } yield LEdge(x, y, a)

  def genExistingEdge[B: Arbitrary, N, A](nodes: List[LNode[N,A]]): Gen[LEdge[N,B]] = for {
    label <- arbitrary[B]
    if nodes.nonEmpty
    a <- Gen.oneOf(nodes)
    b <- Gen.oneOf(nodes)
  } yield LEdge(a.vertex, b.vertex, label)

  def genContext[N: Arbitrary, A: Arbitrary, B: Arbitrary]: Gen[Context[N,A,B]] = for {
    ins <- arbitrary[Vector[(B, N)]]
    outs <- arbitrary[Vector[(B, N)]]
    n <- arbitrary[N]
    a <- arbitrary[A]
  } yield Context(ins, n, a, outs)

  implicit def arbitraryContext[A: Arbitrary, B: Arbitrary, N: Arbitrary] = Arbitrary(genContext[N,A,B])
  implicit def arbitraryEdge[A: Arbitrary, N: Arbitrary] = Arbitrary(genEdge[N,A])
  implicit def arbitraryNode[A: Arbitrary, N: Arbitrary] = Arbitrary(genNode[N,A])
  implicit def arbitraryGraph[A: Arbitrary, B: Arbitrary, N: Arbitrary] =
    Arbitrary(graphGen[N,A,B])

  def genGDecomp[N: Arbitrary, A: Arbitrary, B: Arbitrary]: Gen[GDecomp[N, A, B]] = for {
    g <- graphGen[N, A, B] if !g.isEmpty
    n <- Gen.oneOf(g.nodes)
  } yield g.decomp(n).toGDecomp.get

  implicit def arbitraryGDecomp[A: Arbitrary, B: Arbitrary, N: Arbitrary]: Arbitrary[GDecomp[N, A, B]] = Arbitrary(genGDecomp[N,A,B])
  implicit def arbitraryGDecompF[A: Arbitrary, B: Arbitrary, N: Arbitrary]: Arbitrary[GDecomp[N, A, B] => A] = Arbitrary(Gen.const(_.label))

  def genTwoPointedGraph[N: Arbitrary, A: Arbitrary, B: Arbitrary]: Gen[(Graph[N,A,B], LNode[N,A], LNode[N,A])] = for {
    vs <- Gen.listOf(genNode[N,A])
    if vs.nonEmpty
    a <- Gen.oneOf(vs)
    b <- Gen.oneOf(vs)
    es <- Gen.listOf(genExistingEdge[B,N,A](vs))
  } yield (safeMkGraph(vs, es), a, b)

  implicit def arbitraryTwoPointedGraph[A: Arbitrary, B: Arbitrary, N: Arbitrary]: Arbitrary[(Graph[N,A,B], LNode[N,A], LNode[N,A])] = Arbitrary(genTwoPointedGraph[N,A,B])
}
