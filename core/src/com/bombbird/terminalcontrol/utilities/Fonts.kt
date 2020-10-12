package com.bombbird.terminalcontrol.utilities

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.bombbird.terminalcontrol.TerminalControl

object Fonts {
    private var defaultFont: FreeTypeFontGenerator? = FreeTypeFontGenerator(Gdx.files.internal("fonts/default.ttf"))
    @JvmField
    var defaultFont6: BitmapFont? = generateFont(defaultFont, 24, -1)
    @JvmField
    var compressedFont6: BitmapFont? = generateFont(defaultFont, 24, 26)
    @JvmField
    var expandedFont6: BitmapFont? = generateFont(defaultFont, 24, 40)
    @JvmField
    var defaultFont8: BitmapFont? = generateFont(defaultFont, 32, -1)
    @JvmField
    var defaultFont10: BitmapFont? = generateFont(defaultFont, 40, -1)
    @JvmField
    var defaultFont12: BitmapFont? = generateFont(defaultFont, 48, -1)
    @JvmField
    var defaultFont16: BitmapFont? = generateFont(defaultFont, 64, -1)
    @JvmField
    var defaultFont20: BitmapFont? = generateFont(defaultFont, 80, -1)
    @JvmField
    var defaultFont24: BitmapFont? = generateFont(defaultFont, 96, -1)
    @JvmField
    var defaultFont30: BitmapFont? = generateFont(defaultFont, 120, -1)

    @JvmStatic
    fun generateAllFonts() {
        defaultFont = FreeTypeFontGenerator(Gdx.files.internal("fonts/default.ttf"))
        defaultFont6 = generateFont(defaultFont, 24, -1)
        compressedFont6 = generateFont(defaultFont, 24, 26)
        expandedFont6 = generateFont(defaultFont, 24, 40)
        defaultFont8 = generateFont(defaultFont, 32, -1)
        defaultFont10 = generateFont(defaultFont, 40, -1)
        defaultFont12 = generateFont(defaultFont, 48, -1)
        defaultFont16 = generateFont(defaultFont, 64, -1)
        defaultFont20 = generateFont(defaultFont, 80, -1)
        defaultFont24 = generateFont(defaultFont, 96, -1)
        defaultFont30 = generateFont(defaultFont, 120, -1)
    }

    private fun generateFont(generator: FreeTypeFontGenerator?, size: Int, lineHeight: Int): BitmapFont? {
        if (generator == null) return null
        val parameter = FreeTypeFontGenerator.FreeTypeFontParameter()
        parameter.size = size
        parameter.minFilter = Texture.TextureFilter.Nearest
        parameter.magFilter = Texture.TextureFilter.MipMapLinearNearest
        generator.scaleForPixelHeight(size)
        val font = generator.generateFont(parameter)
        font.data.markupEnabled = true
        if (lineHeight > -1) font.data.setLineHeight(lineHeight.toFloat())
        return font
    }

    @JvmStatic
    fun dispose() {
        defaultFont?.dispose()
        defaultFont6?.dispose()
        compressedFont6?.dispose()
        expandedFont6?.dispose()
        defaultFont8?.dispose()
        defaultFont10?.dispose()
        defaultFont12?.dispose()
        defaultFont16?.dispose()
        defaultFont20?.dispose()
        defaultFont24?.dispose()
        defaultFont30?.dispose()

        defaultFont = null
        defaultFont6 = null
        compressedFont6 = null
        expandedFont6 = null
        defaultFont8 = null
        defaultFont10 = null
        defaultFont12 = null
        defaultFont16 = null
        defaultFont20 = null
        defaultFont24 = null
        defaultFont30 = null
    }
}