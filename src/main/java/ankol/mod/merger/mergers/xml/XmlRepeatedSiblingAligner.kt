package ankol.mod.merger.mergers.xml

import ankol.mod.merger.mergers.RepeatedSiblingAligner
import ankol.mod.merger.mergers.SiblingContent
import ankol.mod.merger.mergers.xml.node.XmlContainerNode
import ankol.mod.merger.mergers.xml.node.XmlNode
import java.util.*

/**
 * Sibling elements with the same signature, such as the 30 <level id="1"> of Dying Light 1's default_levels.xml,
 * cannot be matched by their signature: they are paired by [RepeatedSiblingAligner].
 *
 * The result is a view of the children of each container in which the same key means the same element:
 * an aligned key for those siblings and the signature for every other element, in the order of the container.
 */
internal object XmlRepeatedSiblingAligner {
    class AlignedChildren(
        val vanilla: Map<String, XmlNode>?,
        val accumulated: Map<String, XmlNode>,
        val incoming: Map<String, XmlNode>,
    )

    fun align(
        vanilla: XmlContainerNode?,
        accumulated: XmlContainerNode,
        incoming: XmlContainerNode,
    ): AlignedChildren {
        val containers = listOfNotNull(vanilla, accumulated, incoming)
        if (containers.none { it.hasRepeatedSignatures }) {
            return AlignedChildren(vanilla?.childrens, accumulated.childrens, incoming.childrens)
        }

        val vanillaFamilies = vanilla?.let(::families)
        val accumulatedFamilies = families(accumulated)
        val incomingFamilies = families(incoming)
        // a signature repeated in any version is aligned in all of them
        val repeated = LinkedHashSet<String>()
        for (families in listOfNotNull(vanillaFamilies, accumulatedFamilies, incomingFamilies)) {
            families.forEach { (signature, members) -> if (members.size > 1) repeated += signature }
        }
        // a mark that no signature contains, so that an aligned key cannot be mistaken for a signature
        var mark = "#"
        while (containers.any { container -> container.childrens.values.any { mark in it.signature } }) mark += "#"

        val keys = IdentityHashMap<XmlNode, String>()
        for (signature in repeated) {
            val vanillaMembers = vanillaFamilies?.let { it[signature].orEmpty() }
            val accumulatedMembers = accumulatedFamilies[signature].orEmpty()
            val incomingMembers = incomingFamilies[signature].orEmpty()
            val ids = RepeatedSiblingAligner.align(
                vanillaMembers?.map(::content), accumulatedMembers.map(::content), incomingMembers.map(::content)
            )
            // the first one keeps the signature as its key, as when it is the only one
            fun keyOf(id: String) = if (id == "0") signature else signature + mark + id
            vanillaMembers?.forEachIndexed { index, node -> keys[node] = keyOf(ids.vanilla[index]) }
            accumulatedMembers.forEachIndexed { index, node -> keys[node] = keyOf(ids.accumulated[index]) }
            incomingMembers.forEachIndexed { index, node -> keys[node] = keyOf(ids.incoming[index]) }
        }

        fun view(container: XmlContainerNode): Map<String, XmlNode> {
            val result = LinkedHashMap<String, XmlNode>()
            for (node in container.childrens.values) result[keys[node] ?: node.signature] = node
            return result
        }
        return AlignedChildren(vanilla?.let(::view), view(accumulated), view(incoming))
    }

    private fun families(container: XmlContainerNode): Map<String, List<XmlNode>> =
        container.childrens.values.groupBy { it.signature }

    private fun content(node: XmlNode): SiblingContent {
        val parts by lazy(LazyThreadSafetyMode.NONE) {
            val values = ArrayList<String>()
            val shape = ArrayList<String>()
            // the attributes in the signature are the same in all these siblings, so they do not tell them apart
            val identifying = TechlandXmlFileVisitor.identifyingAttributeName(node.attributes.keys)
            if (identifying != null) {
                for ((name, value) in node.attributes) {
                    if (name == identifying) continue
                    values += "@$name=$value"
                    shape += ownAttribute(node, "@$name", value)
                }
            }
            collect(node, values, shape)
            values to shape
        }
        return SiblingContent(withoutWhitespace(node.sourceText), null, { parts.first }, { parts.second })
    }

    /**
     * Every element inside, however deep: its signature is part of the shape of the sibling, and each of its
     * attributes is one of its values, so that an element whose value changed still has its other attributes to be
     * recognised by.
     */
    private fun collect(node: XmlNode, values: MutableList<String>, shape: MutableList<String>) {
        if (node !is XmlContainerNode) return
        for (child in node.childrens.values) {
            shape += child.signature
            if (child.attributes.isEmpty()) {
                if (child !is XmlContainerNode) values += withoutWhitespace(child.sourceText)
            } else {
                // without an identifying attribute, all the attributes are already in the signature
                val identifying = TechlandXmlFileVisitor.identifyingAttributeName(child.attributes.keys)
                if (identifying != null) {
                    for ((name, value) in child.attributes) {
                        if (name == identifying) continue
                        values += "${child.signature}@$name=$value"
                        if (child is XmlContainerNode) shape += ownAttribute(child, "${child.signature}@$name", value)
                    }
                }
            }
            collect(child, values, shape)
        }
    }

    /**
     * An element with children keeps its own attributes in a merge (only its children are merged), so their
     * values are part of its shape; an element without children is replaced whole.
     */
    private fun ownAttribute(node: XmlNode, name: String, value: String): String =
        if (node is XmlContainerNode) "$name=$value" else name

    private fun withoutWhitespace(text: String): String = text.filterNot(Char::isWhitespace)
}
