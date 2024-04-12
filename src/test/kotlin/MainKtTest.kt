import me.janpulkowski.remitly.getResource
import me.janpulkowski.remitly.isNotStar
import me.janpulkowski.remitly.verify
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Nested
import java.util.Optional
import java.util.Optional.empty
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MainKtTest {
    companion object {
        private val STAR_OPTIONAL = Optional.of("*")
    }

    @Nested
    inner class IsNotStar {
        @Test
        fun `detect a star`() {
            val star = STAR_OPTIONAL
            val expected = false
            assertEquals(expected, isNotStar(star))
        }

        @Test
        fun `detect a string which isn't a star`() {
            val notStar = Optional.of("definitely-not-a-star.csv")
            val expected = true
            assertEquals(expected, isNotStar(notStar))
        }

        @Test
        fun `detect an empty optional`() {
            val empty: Optional<String> = empty()
            val expected = true
            assertEquals(expected, isNotStar(empty))
        }
    }

    @Nested
    inner class GetResource {

        @Test
        fun `should detect a star in resources`() {
                val json = """
                {
                    "PolicyName": "root",
                    "PolicyDocument": {
                        "Version": "2012-10-17",
                        "Statement": [
                            {
                                "Sid": "IamListAccess",
                                "Effect": "Allow",
                                "Action": [
                                    "iam:ListRoles",
                                    "iam:ListUsers"
                                ],
                                "Resource": "*"
                            }
                        ]
                    }
                }
            """.trimIndent()
            val expected = STAR_OPTIONAL
            assertEquals(expected, getResource(json))
        }

        @Test
        fun `should also work for a statement outside of an array`() {
            val json = """
                {
                    "PolicyName": "root",
                    "PolicyDocument": {
                        "Version": "2012-10-17",
                        "Statement": {
                            "Sid": "IamListAccess",
                            "Effect": "Allow",
                            "Action": [
                                "iam:ListRoles",
                                "iam:ListUsers"
                            ],
                            "Resource": "*"
                        }
                    }
                }
            """.trimIndent()
            val expected = STAR_OPTIONAL
            assertEquals(expected, getResource(json))
        }

        @Test
        fun `should detect another string of resources`() {
            val json = """
                {
                    "PolicyName": "root",
                    "PolicyDocument": {
                        "Version": "2012-10-17",
                        "Statement": [{
                            "Sid": "IamListAccess",
                            "Effect": "Allow",
                            "Action": [
                                "iam:ListRoles",
                                "iam:ListUsers"
                            ],
                            "Resource": "bank_account"
                        }]
                    }
                }
            """.trimIndent()
            val result = getResource(json)
            assertTrue(result.isPresent)
        }

        @Test
        fun `should detect list of resources`() {
            val json = """
                {
                    "PolicyName": "root",
                    "PolicyDocument": {
                        "Version": "2012-10-17",
                        "Statement": [{
                            "Sid": "IamListAccess",
                            "Effect": "Allow",
                            "Action": [
                                "iam:ListRoles",
                                "iam:ListUsers"
                            ],
                            "Resource": ["bank_account", "fridge"]
                        }]
                    }
                }
            """.trimIndent()
            val expected: Optional<String> = empty()
            assertEquals(expected, getResource(json))
        }

        @Test
        fun `should detect list of resources if it contains only a star`() {
            val json = """
                {
                    "PolicyName": "root",
                    "PolicyDocument": {
                        "Version": "2012-10-17",
                        "Statement": [{
                            "Sid": "IamListAccess",
                            "Effect": "Allow",
                            "Action": [
                                "iam:ListRoles",
                                "iam:ListUsers"
                            ],
                            "Resource": ["*"]
                        }]
                    }
                }
            """.trimIndent()
            val expected: Optional<String> = STAR_OPTIONAL
            assertEquals(expected, getResource(json))
        }


        @Test
        fun `should detect list of resources if it contains a star and more elements`() {
            val json = """
                {
                    "PolicyName": "root",
                    "PolicyDocument": {
                        "Version": "2012-10-17",
                        "Statement": [{
                            "Sid": "IamListAccess",
                            "Effect": "Allow",
                            "Action": [
                                "iam:ListRoles",
                                "iam:ListUsers"
                            ],
                            "Resource": ["*", "bank"]
                        }]
                    }
                }
            """.trimIndent()
            val expected: Optional<String> = empty()
            assertEquals(expected, getResource(json))
        }

        @Test
        fun `should detect that Resource field is missing`() {
            val json = """
                {
                    "PolicyName": "root",
                    "PolicyDocument": {
                        "Version": "2012-10-17",
                        "Statement": [{
                            "Sid": "IamListAccess",
                            "Effect": "Allow",
                            "Action": [
                                "iam:ListRoles",
                                "iam:ListUsers"
                            ]
                        }]
                    }
                }
            """.trimIndent()
            val expected: Optional<String> = empty()
            assertEquals(expected, getResource(json))
        }

        @Test
        fun `should detect that Statement list is empty`() {
            val json = """
                {
                    "PolicyName": "root",
                    "PolicyDocument": {
                        "Version": "2012-10-17",
                        "Statement": []
                    }
                }
            """.trimIndent()
            val expected: Optional<String> = empty()
            assertEquals(expected, getResource(json))
        }
    }

    @Nested
    inner class Verify {

        @Test
        fun `should detect a star in resources`() {
            val json = """
                {
                    "PolicyName": "root",
                    "PolicyDocument": {
                        "Version": "2012-10-17",
                        "Statement": [
                            {
                                "Sid": "IamListAccess",
                                "Effect": "Allow",
                                "Action": [
                                    "iam:ListRoles",
                                    "iam:ListUsers"
                                ],
                                "Resource": "*"
                            }
                        ]
                    }
                }
            """.trimIndent()
            assertFalse(verify(json))
        }

        @Test
        fun `should also work for a statement outside of an array`() {
            val json = """
                {
                    "PolicyName": "root",
                    "PolicyDocument": {
                        "Version": "2012-10-17",
                        "Statement": {
                            "Sid": "IamListAccess",
                            "Effect": "Allow",
                            "Action": [
                                "iam:ListRoles",
                                "iam:ListUsers"
                            ],
                            "Resource": "*"
                        }
                    }
                }
            """.trimIndent()
            assertFalse(verify(json))
        }

        @Test
        fun `should detect another string of resources`() {
            val json = """
                {
                    "PolicyName": "root",
                    "PolicyDocument": {
                        "Version": "2012-10-17",
                        "Statement": [{
                            "Sid": "IamListAccess",
                            "Effect": "Allow",
                            "Action": [
                                "iam:ListRoles",
                                "iam:ListUsers"
                            ],
                            "Resource": "bank_account"
                        }]
                    }
                }
            """.trimIndent()
            assertTrue(verify(json))
        }

        @Test
        fun `should detect list of resources`() {
            val json = """
                {
                    "PolicyName": "root",
                    "PolicyDocument": {
                        "Version": "2012-10-17",
                        "Statement": [{
                            "Sid": "IamListAccess",
                            "Effect": "Allow",
                            "Action": [
                                "iam:ListRoles",
                                "iam:ListUsers"
                            ],
                            "Resource": ["bank_account", "fridge"]
                        }]
                    }
                }
            """.trimIndent()
            assertTrue(verify(json))
        }

        @Test
        fun `should detect list of resources if it contains only a star`() {
            val json = """
                {
                    "PolicyName": "root",
                    "PolicyDocument": {
                        "Version": "2012-10-17",
                        "Statement": [{
                            "Sid": "IamListAccess",
                            "Effect": "Allow",
                            "Action": [
                                "iam:ListRoles",
                                "iam:ListUsers"
                            ],
                            "Resource": ["*"]
                        }]
                    }
                }
            """.trimIndent()
            assertFalse(verify(json))
        }


        @Test
        fun `should detect list of resources if it contains a star and more elements`() {
            val json = """
                {
                    "PolicyName": "root",
                    "PolicyDocument": {
                        "Version": "2012-10-17",
                        "Statement": [{
                            "Sid": "IamListAccess",
                            "Effect": "Allow",
                            "Action": [
                                "iam:ListRoles",
                                "iam:ListUsers"
                            ],
                            "Resource": ["*", "bank"]
                        }]
                    }
                }
            """.trimIndent()
            assertTrue(verify(json))
        }

        @Test
        fun `should detect that Resource field is missing`() {
            val json = """
                {
                    "PolicyName": "root",
                    "PolicyDocument": {
                        "Version": "2012-10-17",
                        "Statement": [{
                            "Sid": "IamListAccess",
                            "Effect": "Allow",
                            "Action": [
                                "iam:ListRoles",
                                "iam:ListUsers"
                            ]
                        }]
                    }
                }
            """.trimIndent()
            assertTrue(verify(json))
        }

        @Test
        fun `should detect that Statement list is empty`() {
            val json = """
                {
                    "PolicyName": "root",
                    "PolicyDocument": {
                        "Version": "2012-10-17",
                        "Statement": []
                    }
                }
            """.trimIndent()
            assertTrue(verify(json))
        }
    }
}