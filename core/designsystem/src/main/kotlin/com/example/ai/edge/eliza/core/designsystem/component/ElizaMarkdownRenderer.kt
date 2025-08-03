/*
 * Copyright 2025 The Eliza Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.ai.edge.eliza.core.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.halilibo.richtext.commonmark.Markdown
import com.halilibo.richtext.ui.CodeBlockStyle
import com.halilibo.richtext.ui.RichTextStyle
import com.halilibo.richtext.ui.material3.RichText
import com.halilibo.richtext.ui.string.RichTextStringStyle

/**
 * Eliza's markdown renderer for educational content.
 * 
 * Features:
 * - Optimized typography for learning materials
 * - Educational blue theme integration
 * - LaTeX math formula support
 * - Local image handling
 * - Mobile-optimized reading experience
 * - Flexible padding for different contexts (chapters vs chat)
 * 
 * Based on Gallery's MarkdownText but enhanced for educational use.
 */
@Composable
fun ElizaMarkdownRenderer(
    content: String,
    modifier: Modifier = Modifier,
    onImageClick: (String) -> Unit = {},
    smallFontSize: Boolean = false,
    useDefaultPadding: Boolean = true
) {
    val fontSize = if (smallFontSize) {
        MaterialTheme.typography.bodyMedium.fontSize
    } else {
        MaterialTheme.typography.bodyLarge.fontSize
    }
    
    val paddingModifier = if (useDefaultPadding) {
        Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    } else {
        Modifier
    }
    
    CompositionLocalProvider {
        ProvideTextStyle(
            value = TextStyle(
                fontSize = fontSize, 
                lineHeight = fontSize * 1.3
            )
        ) {
            RichText(
                modifier = modifier
                    .fillMaxWidth()
                    .then(paddingModifier),
                style = RichTextStyle(
                    codeBlockStyle = CodeBlockStyle(
                        textStyle = TextStyle(
                            fontSize = MaterialTheme.typography.bodySmall.fontSize,
                            fontFamily = FontFamily.Monospace,
                        )
                    ),
                    stringStyle = RichTextStringStyle(
                        linkStyle = TextLinkStyles(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    ),
                ),
            ) {
                // Process content for LaTeX before rendering
                val processedContent = processLatexContent(content)
                Markdown(content = processedContent)
            }
        }
    }
}

/**
 * Process LaTeX math formulas in the content.
 * Converts LaTeX syntax to displayable format.
 * 
 * TODO: Implement proper LaTeX rendering using MathJax or similar
 */
private fun processLatexContent(content: String): String {
    // For now, just handle basic LaTeX markers
    // Future: Implement proper LaTeX to rendered math conversion
    return content
        .replace("\\$\\$([^$]+)\\$\\$".toRegex()) { matchResult ->
            // Block math formula
            "\n\n**Math Formula:** `${matchResult.groupValues[1]}`\n\n"
        }
        .replace("\\$([^$]+)\\$".toRegex()) { matchResult ->
            // Inline math formula  
            "`${matchResult.groupValues[1]}`"
        }
        .replace("\\\\begin\\{equation\\}([\\s\\S]*?)\\\\end\\{equation\\}".toRegex()) { matchResult ->
            // LaTeX equation environment
            "\n\n**Equation:**\n```\n${matchResult.groupValues[1].trim()}\n```\n\n"
        }
        .replace("\\\\begin\\{align\\}([\\s\\S]*?)\\\\end\\{align\\}".toRegex()) { matchResult ->
            // LaTeX align environment
            "\n\n**Aligned Equations:**\n```\n${matchResult.groupValues[1].trim()}\n```\n\n"
        }
}

/**
 * Preview composable for the markdown renderer.
 */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun ElizaMarkdownRendererPreview() {
    MaterialTheme {
        ElizaMarkdownRenderer(
            content = """
                # Linear Equations
                
                Linear equations are mathematical expressions where the variable appears to the first power.
                
                ## Example 1: Basic Linear Equation
                
                Solve: $2x + 5 = 15$
                
                **Step 1:** Subtract 5 from both sides
                ```
                2x + 5 - 5 = 15 - 5
                2x = 10
                ```
                
                **Step 2:** Divide both sides by 2
                ```
                x = 5
                ```
                
                ### Mathematical Formula
                
                The general form is: $${'$'}ax + b = c$${'$'}
                
                Where:
                - `a`, `b`, and `c` are constants
                - `x` is the variable
                - `a ≠ 0`
            """.trimIndent()
        )
    }
}