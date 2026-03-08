package io.github.pylonmc.rebar.i18n

import com.google.common.base.Preconditions
import io.github.pylonmc.rebar.config.RebarConfig
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration

object LineWrapping {

    /**
     * The root component which has all the other components in
     * a line as children
     */
    val DEFAULT_COMPONENT = Component.empty()
        .decoration(TextDecoration.ITALIC, false)
        .color(NamedTextColor.GRAY)

    /**
     * Splits a [Component] into multiple components with `\n` as a delimeter.
     *
     * Only considers newlines inside [TextComponent]s
     */
    fun splitByNewlines(component: Component): List<Component> {
        val lines = mutableListOf<Component>()
        val currentLine = DEFAULT_COMPONENT
        lines.add(splitByNewlines(lines, currentLine, component.style(), component))
        return lines
    }

    fun splitByNewlines(lines: MutableList<Component>, currentLine: Component, style: Style, component: Component): Component {
        var currentLine = currentLine

        // Handle component contents
        if (component is TextComponent) {
            // Text component could have a newline or multiple newlines in it
            val split = component.content().split('\n')
            Preconditions.checkState(!split.isEmpty())
            for (text in split.dropLast(1)) {
                currentLine = currentLine.append(Component.text(text).style(style))
                lines.add(currentLine)
                currentLine = DEFAULT_COMPONENT
            }
            currentLine = currentLine.append(Component.text(split.last()).style(style))
        } else {
            // Add JUST the component contents, and not any of its children
            currentLine = currentLine.append(component.children(mutableListOf<Component>()))
        }

        // Handle children
        for (child in component.children()) {
            currentLine = splitByNewlines(lines, currentLine, style.merge(child.style()), child)
        }

        return currentLine
    }

    /**
     * Splits a single [Component] into multiple lines, aiming to limit each line's
     * length to [RebarConfig.TRANSLATION_WRAP_LIMIT]
     *
     * Only considers newlines inside [TextComponent]s
     */
    fun wrapLine(component: Component): List<Component> {
        val lines = mutableListOf<Component>()
        val currentLine = DEFAULT_COMPONENT
        val currentLineLength = 0
        val result = wrapLine(lines, currentLine, currentLineLength, component.style(), component)
        lines.add(result.first)
        return lines
    }

    fun wrapLine(
        lines: MutableList<Component>,
        currentLine: Component,
        currentLineLength: Int,
        style: Style,
        component: Component
    ): Pair<Component, Int> {
        var currentLine = currentLine
        var currentLineLength = currentLineLength

        // Handle component contents
        if (component is TextComponent) {
            // Text component has stuff (text) that we can split up. Other types of components don't.
            // So line wrapping might be wrong for certain components (e.g. translateable components
            // which are handled clientside) but this is fine, it can't be perfect.
            var content = component.content()
            while (currentLineLength + content.length > RebarConfig.TRANSLATION_WRAP_LIMIT) {

                // Make sure we snap to the end of a word (i.e. don't cut words in half)
                var endIndex = RebarConfig.TRANSLATION_WRAP_LIMIT - currentLineLength
                while (endIndex != 0 && content[endIndex] != ' ') {
                    endIndex -= 1
                }

                currentLine = currentLine.append(Component.text(content.substring(0, endIndex)).style(style))
                lines.add(currentLine)
                currentLine = DEFAULT_COMPONENT
                currentLineLength = 0
                content = if (endIndex+1 == content.length) "" else content.substring(endIndex+1)
            }
            currentLine = currentLine.append(Component.text(content).style(style))
            currentLineLength += content.length

        } else {
            // Treat as zero-length
            // Add JUST the component contents, and not any of its children
            currentLine = currentLine.append(component.children(mutableListOf<Component>()))
        }

        // Handle children
        for (child in component.children()) {
            val result = wrapLine(lines, currentLine, currentLineLength, style.merge(child.style()), child)
            currentLine = result.first
            currentLineLength = result.second
        }

        return Pair(currentLine, currentLineLength)
    }
}