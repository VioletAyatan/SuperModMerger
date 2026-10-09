package ankol.mod.merger.rules

import ankol.mod.merger.exception.BusinessException
import ankol.mod.merger.constants.ScrNodeType
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MergeRulesTest {
    private fun target(
        path: String = "scripts/inventory/inventory_special.scr",
        type: ScrNodeType = ScrNodeType.FUN_CALL,
        name: String = "Dlc",
        argCount: Int? = 1,
    ) = DeletionTarget(path, type, name, argCount)

    @Test
    fun builtinRulesDeleteInventoryDlcAndRestoreEverythingElse() {
        val policy = MergeRules.builtinPolicy
        assertEquals(RuleAction.RESTORE, policy.defaultAction)
        assertEquals(RuleAction.DELETE, policy.decide(target()))
        assertEquals(RuleAction.DELETE, policy.decide(target(path = "Scripts\\Inventory\\Inventory_DLC.scr")))
        assertEquals(RuleAction.RESTORE, policy.decide(target(name = "Item")))
        assertEquals(RuleAction.RESTORE, policy.decide(target(type = ScrNodeType.METHOD_REFERENCE)))
        assertEquals(RuleAction.RESTORE, policy.decide(target(path = "scripts/player/player_variables.scr")))
    }

    @Test
    fun laterRulesOverrideEarlierRulesAndExternalDefaultOverridesBuiltin() {
        val builtin = MergeRules.parse(
            """
            {"scr": {"deletion": {"defaultAction": "RESTORE", "rules": [
                {"file": "**/*.scr", "nodeType": "funCall", "action": "DELETE"}
            ]}}}
            """.trimIndent(), "builtin"
        )
        val external = MergeRules.parse(
            """
            {
              // comments and trailing commas are allowed
              "scr": {"deletion": {"defaultAction": "ask", "rules": [
                {"file": "scripts/player/**", "name": "Param*", "action": "RESTORE"},
              ]}}
            }
            """.trimIndent(), "external"
        )
        val policy = MergeRules.buildPolicy(listOf("builtin" to builtin, "external" to external))

        assertEquals(RuleAction.ASK, policy.defaultAction)
        assertEquals(RuleAction.DELETE, policy.decide(target(path = "scripts/player/player_variables.scr", name = "Health")))
        assertEquals(RuleAction.RESTORE, policy.decide(target(path = "scripts/player/player_variables.scr", name = "ParamFloat")))
        assertEquals(RuleAction.ASK, policy.decide(target(type = ScrNodeType.SUB, name = "main", argCount = null)))
    }

    @Test
    fun externalLayerWithoutDefaultKeepsBuiltinDefault() {
        val builtin = MergeRules.parse("""{"scr":{"deletion":{"defaultAction":"DELETE"}}}""", "builtin")
        val external = MergeRules.parse("""{"scr":{"deletion":{"rules":[]}}}""", "external")
        assertEquals(RuleAction.DELETE, MergeRules.buildPolicy(listOf("b" to builtin, "e" to external)).defaultAction)
        assertEquals(RuleAction.RESTORE, MergeRules.buildPolicy(emptyList()).defaultAction)
    }

    @Test
    fun argCountOnlyMatchesNodesWithThatManyArguments() {
        val config = MergeRules.parse(
            """{"scr":{"deletion":{"rules":[{"file":"**","nodeType":"funCall","argCount":2,"action":"DELETE"}]}}}""",
            "test"
        )
        val policy = MergeRules.buildPolicy(listOf("test" to config))
        assertEquals(RuleAction.DELETE, policy.decide(target(argCount = 2)))
        assertEquals(RuleAction.RESTORE, policy.decide(target(argCount = 1)))
        assertEquals(RuleAction.RESTORE, policy.decide(target(type = ScrNodeType.VARIABLE, argCount = null)))
    }

    @Test
    fun invalidConfigurationIsRejected() {
        assertFailsWith<BusinessException> {
            MergeRules.buildPolicy(listOf("t" to MergeRules.parse(
                """{"scr":{"deletion":{"rules":[{"file":"**","nodeType":"function","action":"DELETE"}]}}}""", "t"
            )))
        }
        assertFailsWith<BusinessException> {
            MergeRules.buildPolicy(listOf("t" to MergeRules.parse(
                """{"scr":{"deletion":{"rules":[{"file":" ","action":"DELETE"}]}}}""", "t"
            )))
        }
        assertFailsWith<BusinessException> {
            MergeRules.parse("""{"scr":{"deletion":{"rules":[{"file":"**","action":"REMOVE"}]}}}""", "t")
        }
        assertFailsWith<BusinessException> {
            MergeRules.parse("""{"scr":{"deletion":{"rulez":[]}}}""", "t")
        }
    }
}
