package com.mapbox.navigation.ui.route

import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import androidx.test.core.app.ApplicationProvider
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.core.constants.Constants
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.libnavigation.ui.R
import com.mapbox.mapboxsdk.location.LocationComponentConstants
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.Layer
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.navigation.ui.ThemeSwitcher
import com.mapbox.navigation.ui.route.RouteConstants.ALTERNATIVE_ROUTE_LAYER_ID
import com.mapbox.navigation.ui.route.RouteConstants.ALTERNATIVE_ROUTE_SHIELD_LAYER_ID
import com.mapbox.navigation.ui.route.RouteConstants.PRIMARY_ROUTE_LAYER_ID
import com.mapbox.navigation.ui.route.RouteConstants.PRIMARY_ROUTE_SHIELD_LAYER_ID
import com.mapbox.navigation.ui.route.RouteConstants.WAYPOINT_LAYER_ID
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MapRouteLineTest {

    lateinit var ctx: Context
    var styleRes: Int = 0
    lateinit var wayPointSource: GeoJsonSource
    lateinit var primaryRouteLineSource: GeoJsonSource
    lateinit var alternativeRouteLineSource: GeoJsonSource

    lateinit var mapRouteSourceProvider: MapRouteSourceProvider
    lateinit var layerProvider: MapRouteLayerProvider
    lateinit var alternativeRouteShieldLayer: LineLayer
    lateinit var alternativeRouteLayer: LineLayer
    lateinit var primaryRouteShieldLayer: LineLayer
    lateinit var primaryRouteLayer: LineLayer
    lateinit var waypointLayer: SymbolLayer

    lateinit var style: Style

    @Before
    fun setUp() {
        ctx = ApplicationProvider.getApplicationContext()
        styleRes = ThemeSwitcher.retrieveAttrResourceId(
            ctx,
            R.attr.navigationViewRouteStyle, R.style.NavigationMapRoute
        )
        alternativeRouteShieldLayer = mockk {
            every { id } returns ALTERNATIVE_ROUTE_SHIELD_LAYER_ID
        }

        alternativeRouteLayer = mockk {
            every { id } returns ALTERNATIVE_ROUTE_LAYER_ID
        }

        primaryRouteShieldLayer = mockk {
            every { id } returns PRIMARY_ROUTE_SHIELD_LAYER_ID
        }

        primaryRouteLayer = mockk {
            every { id } returns PRIMARY_ROUTE_LAYER_ID
        }

        waypointLayer = mockk {
            every { id } returns WAYPOINT_LAYER_ID
        }

        style = mockk(relaxUnitFun = true) {
            every { getLayer(ALTERNATIVE_ROUTE_LAYER_ID) } returns alternativeRouteLayer
            every { getLayer(ALTERNATIVE_ROUTE_SHIELD_LAYER_ID) } returns alternativeRouteShieldLayer
            every { getLayer(PRIMARY_ROUTE_LAYER_ID) } returns primaryRouteLayer
            every { getLayer(PRIMARY_ROUTE_SHIELD_LAYER_ID) } returns primaryRouteShieldLayer
            every { getLayer(WAYPOINT_LAYER_ID) } returns waypointLayer
            every { isFullyLoaded } returns false
        }

        wayPointSource = mockk(relaxUnitFun = true)
        primaryRouteLineSource = mockk(relaxUnitFun = true)
        alternativeRouteLineSource = mockk(relaxUnitFun = true)

        mapRouteSourceProvider = mockk {
            every { build(RouteConstants.WAYPOINT_SOURCE_ID, any(), any()) } returns wayPointSource
            every { build(RouteConstants.PRIMARY_ROUTE_SOURCE_ID, any(), any()) } returns primaryRouteLineSource
            every { build(RouteConstants.ALTERNATIVE_ROUTE_SOURCE_ID, any(), any()) } returns alternativeRouteLineSource
        }
        layerProvider = mockk {
            every { initializeAlternativeRouteShieldLayer(style, 1.0f, -9273715) } returns alternativeRouteShieldLayer
            every { initializeAlternativeRouteLayer(style, true, 1.0f, -7957339) } returns alternativeRouteLayer
            every { initializePrimaryRouteShieldLayer(style, 1.0f, -13665594) } returns primaryRouteShieldLayer
            every { initializePrimaryRouteLayer(style, true, 1.0f, -11097861) } returns primaryRouteLayer
            every { initializeWayPointLayer(style, any(), any()) } returns waypointLayer
        }
    }

    @Test
    fun getStyledColor() {
        val result = MapRouteLine.MapRouteLineSupport.getStyledColor(
            R.styleable.NavigationMapRoute_routeColor,
            R.color.mapbox_navigation_route_layer_blue,
            ctx,
            styleRes
        )

        assertEquals(-11097861, result)
    }

    @Test
    fun getPrimaryRoute() {
        every { style.layers } returns listOf(primaryRouteLayer)
        val directionsRoute: DirectionsRoute = getDirectionsRoute(true)
        val mapRouteLine = MapRouteLine(
            ctx,
            style,
            styleRes,
            null,
            layerProvider,
            mapRouteSourceProvider).also { it.draw(listOf(directionsRoute)) }

        val result = mapRouteLine.getPrimaryRoute()

        assertEquals(result, directionsRoute)
    }

    @Test
    fun getLineStringForRoute() {
        every { style.layers } returns listOf(primaryRouteLayer)
        val directionsRoute: DirectionsRoute = getDirectionsRoute(true)
        val mapRouteLine = MapRouteLine(
            ctx,
            style,
            styleRes,
            null,
            layerProvider,
            mapRouteSourceProvider).also { it.draw(listOf(directionsRoute)) }

        val result = mapRouteLine.getLineStringForRoute(directionsRoute)

        assertEquals(result.coordinates().size, 4)
    }

    @Test
    fun getLineStringForRouteWhenCalledWithUnknownRoute() {
        every { style.layers } returns listOf(primaryRouteLayer)
        val directionsRoute: DirectionsRoute = getDirectionsRoute(true)
        val directionsRoute2: DirectionsRoute = getDirectionsRoute(true)
        val mapRouteLine = MapRouteLine(
            ctx,
            style,
            styleRes,
            null,
            layerProvider,
            mapRouteSourceProvider).also { it.draw(listOf(directionsRoute)) }

        val result = mapRouteLine.getLineStringForRoute(directionsRoute2)

        assertNotNull(result)
    }

    @Test
    fun retrieveRouteFeatureData() {
        every { style.layers } returns listOf(primaryRouteLayer)
        val directionsRoute: DirectionsRoute = getDirectionsRoute(true)
        val mapRouteLine = MapRouteLine(
            ctx,
            style,
            styleRes,
            null,
            layerProvider,
            mapRouteSourceProvider).also { it.draw(listOf(directionsRoute)) }

        val result = mapRouteLine.retrieveRouteFeatureData()

        assertEquals(result.size, 1)
        assertEquals(result[0].route, directionsRoute)
    }

    @Test
    fun retrieveRouteLineStrings() {
        every { style.layers } returns listOf(primaryRouteLayer)
        val directionsRoute: DirectionsRoute = getDirectionsRoute(true)
        val mapRouteLine = MapRouteLine(
            ctx,
            style,
            styleRes,
            null,
            layerProvider,
            mapRouteSourceProvider).also { it.draw(listOf(directionsRoute)) }

        val result = mapRouteLine.retrieveRouteLineStrings()

        assertEquals(result.size, 1)
    }

    @Test
    fun retrieveDirectionsRoutes() {
        every { style.layers } returns listOf(primaryRouteLayer)
        val directionsRoute: DirectionsRoute = getDirectionsRoute(true)
        val mapRouteLine = MapRouteLine(
            ctx,
            style,
            styleRes,
            null,
            layerProvider,
            mapRouteSourceProvider).also { it.draw(listOf(directionsRoute)) }

        val result = mapRouteLine.retrieveDirectionsRoutes()

        assertEquals(result[0], directionsRoute)
    }

    @Test
    fun retrieveDirectionsRoutesPrimaryRouteIsFirstInList() {
        every { style.layers } returns listOf(primaryRouteLayer)
        val primaryRoute: DirectionsRoute = getDirectionsRoute(true)
        val alternativeRoute: DirectionsRoute = getDirectionsRoute(false)
        val directionsRoutes = mutableListOf(primaryRoute, alternativeRoute)
        val mapRouteLine = MapRouteLine(
            ctx,
            style,
            styleRes,
            null,
            layerProvider,
            mapRouteSourceProvider).also { it.draw(directionsRoutes) }
        directionsRoutes.reverse()

        val result = mapRouteLine.retrieveDirectionsRoutes()

        assertEquals(result[0], primaryRoute)
        assertEquals(2, result.size)
    }

    @Test
    fun retrieveDirectionsRoutesWhenPrimaryRouteIsNull() {
        every { style.layers } returns listOf(primaryRouteLayer)
        val firstRoute: DirectionsRoute = getDirectionsRoute(true)
        val secondRoute: DirectionsRoute = getDirectionsRoute(false)
        val directionsRoutes = listOf(
            RouteFeatureData(firstRoute, mockk<FeatureCollection>(), mockk<LineString>()),
            RouteFeatureData(secondRoute, mockk<FeatureCollection>(), mockk<LineString>()))
        val mapRouteLine = MapRouteLine(
            ctx,
            style,
            styleRes,
            null,
            layerProvider,
            directionsRoutes,
            false,
            false,
            mapRouteSourceProvider)

        val result = mapRouteLine.retrieveDirectionsRoutes()

        assertEquals(2, result.size)
    }

    @Test
    fun getTopLayerId() {
        every { style.layers } returns listOf(primaryRouteLayer)
        val mapRouteLine = MapRouteLine(
            ctx,
            style,
            styleRes,
            null,
            layerProvider,
            mapRouteSourceProvider)

        val result = mapRouteLine.getTopLayerId()

        assertEquals(result, "mapbox-navigation-waypoint-layer")
    }

    @Test
    fun updatePrimaryRouteIndex() {
        every { style.layers } returns listOf(primaryRouteLayer)
        val directionsRoute: DirectionsRoute = getDirectionsRoute(true)
        val directionsRoute2: DirectionsRoute = getDirectionsRoute(true)
        val mapRouteLine = MapRouteLine(
            ctx,
            style,
            styleRes,
            null,
            layerProvider,
            mapRouteSourceProvider).also { it.draw(listOf(directionsRoute, directionsRoute2)) }

        assertEquals(mapRouteLine.getPrimaryRoute(), directionsRoute)

        mapRouteLine.updatePrimaryRouteIndex(directionsRoute2)
        val result = mapRouteLine.getPrimaryRoute()

        assertEquals(result, directionsRoute2)
    }

    @Test
    fun getStyledColorRecyclesAttributes() {
        val context = mockk<Context>()
        val resources = mockk<Resources>()
        val typedArray = mockk<TypedArray>(relaxUnitFun = true)
        every { context.obtainStyledAttributes(styleRes, R.styleable.NavigationMapRoute) } returns typedArray
        every { context.resources } returns resources
        every { context.getColor(R.color.mapbox_navigation_route_layer_blue) } returns 0
        every { resources.getColor(R.color.mapbox_navigation_route_layer_blue) } returns 0
        every { typedArray.getColor(R.styleable.NavigationMapRoute_routeColor, anyInt()) } returns 0

        MapRouteLine.MapRouteLineSupport.getStyledColor(
            R.styleable.NavigationMapRoute_routeColor,
            R.color.mapbox_navigation_route_layer_blue,
            context,
            styleRes
        )

        verify(exactly = 1) { typedArray.recycle() }
    }

    @Test
    fun getFloatStyledValue() {
        val result: Float = MapRouteLine.MapRouteLineSupport.getFloatStyledValue(
            R.styleable.NavigationMapRoute_alternativeRouteScale,
            1.0f,
            ctx,
            styleRes
        )

        assertEquals(1.0f, result)
    }

    @Test
    fun getFloatStyledValueRecyclesAttributes() {
        val context = mockk<Context>()
        val typedArray = mockk<TypedArray>(relaxUnitFun = true)
        every { context.obtainStyledAttributes(styleRes, R.styleable.NavigationMapRoute) } returns typedArray
        every { typedArray.getFloat(R.styleable.NavigationMapRoute_alternativeRouteScale, 1.0f) } returns 1.0f

        MapRouteLine.MapRouteLineSupport.getFloatStyledValue(
            R.styleable.NavigationMapRoute_alternativeRouteScale,
            1.0f,
            context,
            styleRes
        )

        verify(exactly = 1) { typedArray.recycle() }
    }

    @Test
    fun getBooleanStyledValue() {
        val result = MapRouteLine.MapRouteLineSupport.getBooleanStyledValue(
            R.styleable.NavigationMapRoute_roundedLineCap,
            true,
            ctx,
            styleRes
        )

        assertEquals(true, result)
    }

    @Test
    fun getBooleanStyledValueRecyclesAttributes() {
        val context = mockk<Context>()
        val typedArray = mockk<TypedArray>(relaxUnitFun = true)
        every { context.obtainStyledAttributes(styleRes, R.styleable.NavigationMapRoute) } returns typedArray
        every { typedArray.getBoolean(R.styleable.NavigationMapRoute_roundedLineCap, true) } returns true

        MapRouteLine.MapRouteLineSupport.getBooleanStyledValue(
            R.styleable.NavigationMapRoute_roundedLineCap,
            true,
            context,
            styleRes
        )

        verify(exactly = 1) { typedArray.recycle() }
    }

    @Test
    fun getResourceStyledValue() {
        val result = MapRouteLine.MapRouteLineSupport.getResourceStyledValue(
            R.styleable.NavigationMapRoute_originWaypointIcon,
            R.drawable.ic_route_origin,
            ctx,
            styleRes
        )

        assertEquals(R.drawable.ic_route_origin, result)
    }

    @Test
    fun getResourceStyledValueRecyclesAttributes() {
        val context = mockk<Context>()
        val typedArray = mockk<TypedArray>(relaxUnitFun = true)
        every { context.obtainStyledAttributes(styleRes, R.styleable.NavigationMapRoute) } returns typedArray
        every { typedArray.getResourceId(R.styleable.NavigationMapRoute_originWaypointIcon, R.drawable.ic_route_origin) } returns R.drawable.ic_route_origin

        MapRouteLine.MapRouteLineSupport.getResourceStyledValue(
            R.styleable.NavigationMapRoute_originWaypointIcon,
            R.drawable.ic_route_origin,
            context,
            styleRes
        )

        verify(exactly = 1) { typedArray.recycle() }
    }

    @Test
    fun getBelowLayerWithNullLayerId() {
        val style = mockk<Style>()
        val layerApple = mockk<Layer>()
        val layerBanana = mockk<Layer>()
        val layerCantaloupe = mockk<Layer>()
        val layerDragonfruit = mockk<SymbolLayer>()
        val layers = listOf(layerApple, layerBanana, layerCantaloupe, layerDragonfruit)
        every { style.layers } returns layers
        every { layerApple.id } returns "layerApple"
        every { layerBanana.id } returns RouteConstants.MAPBOX_LOCATION_ID
        every { layerCantaloupe.id } returns "layerCantaloupe"
        every { layerDragonfruit.id } returns "layerDragonfruit"

        val result = MapRouteLine.MapRouteLineSupport.getBelowLayer(null, style)

        assertEquals("layerCantaloupe", result)
    }

    @Test
    fun getBelowLayerWithEmptyLayerId() {
        val style = mockk<Style>()
        val layerApple = mockk<Layer>()
        val layerBanana = mockk<Layer>()
        val layerCantaloupe = mockk<Layer>()
        val layerDragonfruit = mockk<SymbolLayer>()
        val layers = listOf(layerApple, layerBanana, layerCantaloupe, layerDragonfruit)
        every { style.layers } returns layers
        every { layerApple.id } returns "layerApple"
        every { layerBanana.id } returns RouteConstants.MAPBOX_LOCATION_ID
        every { layerCantaloupe.id } returns "layerCantaloupe"
        every { layerDragonfruit.id } returns "layerDragonfruit"

        val result = MapRouteLine.MapRouteLineSupport.getBelowLayer("", style)

        assertEquals("layerCantaloupe", result)
    }

    @Test
    fun getBelowLayerReturnsShadowLayerIdAsDefault() {
        val style = mockk<Style>()
        val layerApple = mockk<Layer>()
        val layerBanana = mockk<SymbolLayer>()
        val layers = listOf(layerApple, layerBanana)
        every { style.layers } returns layers
        every { layerApple.id } returns RouteConstants.MAPBOX_LOCATION_ID
        every { layerBanana.id } returns "layerBanana"

        val result = MapRouteLine.MapRouteLineSupport.getBelowLayer(null, style)

        assertEquals(LocationComponentConstants.SHADOW_LAYER, result)
    }

    @Test
    fun getBelowLayerReturnsInputIdIfFound() {
        val style = mockk<Style>()
        val layerApple = mockk<Layer>()
        val layerBanana = mockk<Layer>()
        val layerCantaloupe = mockk<Layer>()
        val layerDragonfruit = mockk<Layer>()
        val layers = listOf(layerApple, layerBanana, layerCantaloupe, layerDragonfruit)
        every { style.layers } returns layers
        every { layerApple.id } returns "layerApple"
        every { layerBanana.id } returns "layerBanana"
        every { layerCantaloupe.id } returns "layerCantaloupe"
        every { layerDragonfruit.id } returns "layerDragonfruit"

        val result = MapRouteLine.MapRouteLineSupport.getBelowLayer("layerBanana", style)

        assertEquals("layerBanana", result)
    }

    @Test
    fun getBelowLayerReturnsShadowLayerIfInputNotNullOrEmptyAndNotFound() {
        val style = mockk<Style>()
        val layerApple = mockk<Layer>()
        val layerBanana = mockk<Layer>()
        val layerCantaloupe = mockk<Layer>()
        val layerDragonfruit = mockk<Layer>()
        val layers = listOf(layerApple, layerBanana, layerCantaloupe, layerDragonfruit)
        every { style.layers } returns layers
        every { layerApple.id } returns "layerApple"
        every { layerBanana.id } returns "layerBanana"
        every { layerCantaloupe.id } returns "layerCantaloupe"
        every { layerDragonfruit.id } returns "layerDragonfruit"

        val result = MapRouteLine.MapRouteLineSupport.getBelowLayer("foobar", style)

        assertEquals(LocationComponentConstants.SHADOW_LAYER, result)
    }

    @Test
    fun generateFeatureCollectionContainsRoute() {
        val route = getDirectionsRoute(true)

        val result = MapRouteLine.MapRouteLineSupport.generateFeatureCollection(route)

        assertEquals(route, result.route)
    }

    @Test
    fun generateFeatureLineStringContainsCorrectCoordinates() {
        val route = getDirectionsRoute(true)

        val result = MapRouteLine.MapRouteLineSupport.generateFeatureCollection(route)

        assertEquals(4, result.lineString.coordinates().size)
    }

    @Test
    fun generateFeatureFeatureCollectionContainsCorrectFeatures() {
        val route = getDirectionsRoute(true)

        val result = MapRouteLine.MapRouteLineSupport.generateFeatureCollection(route)

        assertEquals(1, result.featureCollection.features()!!.size)
    }

    @Test
    fun buildRouteLineExpression() {
        every { style.layers } returns listOf(primaryRouteLayer)
        val expectedExpression = "[\"step\", [\"line-progress\"], [\"rgba\", 0.0, 0.0, 0.0, 0.0], 0.2, [\"rgba\", 86.0, 168.0, 251.0, 1.0], 0.31436133, [\"rgba\", 86.0, 168.0, 251.0, 1.0], 0.66388464, [\"rgba\", 233.0, 51.0, 64.0, 1.0], 0.6948727, [\"rgba\", 86.0, 168.0, 251.0, 1.0]]"
        val route = getDirectionsRoute(true)
        val mapRouteLine = MapRouteLine(
            ctx,
            style,
            styleRes,
            null,
            layerProvider,
            mapRouteSourceProvider).also { it.draw(listOf(route)) }

        val expression = mapRouteLine.getExpressionAtOffset(.2f)

        assertEquals(expectedExpression, expression.toString())
    }

    @Test
    fun buildRouteLineExpressionWhenNoTraffic() {
        every { style.layers } returns listOf(primaryRouteLayer)
        val expectedExpression = "[\"step\", [\"line-progress\"], [\"rgba\", 0.0, 0.0, 0.0, 0.0], 0.2, [\"rgba\", 86.0, 168.0, 251.0, 1.0]]"
        val route = getDirectionsRoute(false)
        val mapRouteLine = MapRouteLine(
            ctx,
            style,
            styleRes,
            null,
            layerProvider,
            mapRouteSourceProvider).also { it.draw(listOf(route)) }

        val expression = mapRouteLine.getExpressionAtOffset(.2f)

        assertEquals(expectedExpression, expression.toString())
    }

    @Test
    fun buildRouteLineExpressionOffsetAfterLastLeg() {
        every { style.layers } returns listOf(primaryRouteLayer)
        val expectedExpression = "[\"step\", [\"line-progress\"], [\"rgba\", 0.0, 0.0, 0.0, 0.0], 0.9, [\"rgba\", 86.0, 168.0, 251.0, 1.0]]"
        val route = getDirectionsRoute(false)
        val mapRouteLine = MapRouteLine(
            ctx,
            style,
            styleRes,
            null,
            layerProvider,
            mapRouteSourceProvider).also { it.draw(listOf(route)) }

        val expression = mapRouteLine.getExpressionAtOffset(.9f)

        assertEquals(expectedExpression, expression.toString())
    }

    @Test
    fun getStopsFromLegReturnsCorrectNumStops() {
        val route = getDirectionsRoute(true)
        val lineString = LineString.fromPolyline(route.geometry()!!, Constants.PRECISION_6)

        val result = MapRouteLine.MapRouteLineSupport.calculateRouteLineSegmentFromLeg(
            route.legs()!![0],
            lineString,
            route.distance()!!,
            true
        ) { _, _ -> 1 }

        assertEquals(4, result.size)
    }

    @Test
    fun buildWayPointFeatureCollection() {
        val route = getDirectionsRoute(true)

        val result = MapRouteLine.MapRouteLineSupport.buildWayPointFeatureCollection(route)

        assertEquals(2, result.features()!!.size)
    }

    @Test
    fun buildWayPointFeatureCollectionFirstFeatureOrigin() {
        val route = getDirectionsRoute(true)

        val result = MapRouteLine.MapRouteLineSupport.buildWayPointFeatureCollection(route)

        assertEquals("{\"wayPoint\":\"origin\"}", result.features()!![0].properties().toString())
    }

    @Test
    fun buildWayPointFeatureCollectionSecondFeatureOrigin() {
        val route = getDirectionsRoute(true)

        val result = MapRouteLine.MapRouteLineSupport.buildWayPointFeatureCollection(route)

        assertEquals("{\"wayPoint\":\"destination\"}", result.features()!![1].properties().toString())
    }

    @Test
    fun buildWayPointFeatureFromLeg() {
        val route = getDirectionsRoute(true)

        val result = MapRouteLine.MapRouteLineSupport.buildWayPointFeatureFromLeg(route.legs()!![0], 0)

        assertEquals(-122.523514, (result!!.geometry() as Point).coordinates()[0], 0.0)
        assertEquals(37.975355, (result.geometry() as Point).coordinates()[1], 0.0)
    }

    @Test
    fun buildWayPointFeatureFromLegContainsOriginWaypoint() {
        val route = getDirectionsRoute(true)

        val result = MapRouteLine.MapRouteLineSupport.buildWayPointFeatureFromLeg(route.legs()!![0], 0)

        assertEquals("\"origin\"", result!!.properties()!!["wayPoint"].toString())
    }

    private fun getDirectionsRoute(includeCongestion: Boolean): DirectionsRoute {
        val congestion = when (includeCongestion) {
            true -> "\"unknown\",\"heavy\",\"low\""
            false -> ""
        }
        val tokenHere = "someToken"
        val directionsRouteAsJson = "{\"routeIndex\":\"0\",\"distance\":66.9,\"duration\":45.0,\"geometry\":\"urylgArvfuhFjJ`CbC{[pAZ\",\"weight\":96.6,\"weight_name\":\"routability\",\"legs\":[{\"distance\":66.9,\"duration\":45.0,\"summary\":\"Laurel Place, Lincoln Avenue\",\"steps\":[{\"distance\":21.0,\"duration\":16.7,\"geometry\":\"urylgArvfuhFjJ`C\",\"name\":\"\",\"mode\":\"driving\",\"maneuver\":{\"location\":[-122.523514,37.975355],\"bearing_before\":0.0,\"bearing_after\":196.0,\"instruction\":\"Head south\",\"type\":\"depart\",\"modifier\":\"right\"},\"voiceInstructions\":[{\"distanceAlongGeometry\":21.0,\"announcement\":\"Head south, then turn left onto Laurel Place\",\"ssmlAnnouncement\":\"\\u003cspeak\\u003e\\u003camazon:effect name\\u003d\\\"drc\\\"\\u003e\\u003cprosody rate\\u003d\\\"1.08\\\"\\u003eHead south, then turn left onto Laurel Place\\u003c/prosody\\u003e\\u003c/amazon:effect\\u003e\\u003c/speak\\u003e\"},{\"distanceAlongGeometry\":18.9,\"announcement\":\"Turn left onto Laurel Place, then turn right onto Lincoln Avenue\",\"ssmlAnnouncement\":\"\\u003cspeak\\u003e\\u003camazon:effect name\\u003d\\\"drc\\\"\\u003e\\u003cprosody rate\\u003d\\\"1.08\\\"\\u003eTurn left onto Laurel Place, then turn right onto Lincoln Avenue\\u003c/prosody\\u003e\\u003c/amazon:effect\\u003e\\u003c/speak\\u003e\"}],\"bannerInstructions\":[{\"distanceAlongGeometry\":21.0,\"primary\":{\"text\":\"Laurel Place\",\"components\":[{\"text\":\"Laurel Place\",\"type\":\"text\",\"abbr\":\"Laurel Pl\",\"abbr_priority\":0}],\"type\":\"turn\",\"modifier\":\"left\"}},{\"distanceAlongGeometry\":18.9,\"primary\":{\"text\":\"Laurel Place\",\"components\":[{\"text\":\"Laurel Place\",\"type\":\"text\",\"abbr\":\"Laurel Pl\",\"abbr_priority\":0}],\"type\":\"turn\",\"modifier\":\"left\"},\"sub\":{\"text\":\"Lincoln Avenue\",\"components\":[{\"text\":\"Lincoln Avenue\",\"type\":\"text\",\"abbr\":\"Lincoln Ave\",\"abbr_priority\":0}],\"type\":\"turn\",\"modifier\":\"right\"}}],\"driving_side\":\"right\",\"weight\":52.6,\"intersections\":[{\"location\":[-122.523514,37.975355],\"bearings\":[196],\"entry\":[true],\"out\":0}]},{\"distance\":41.2,\"duration\":27.3,\"geometry\":\"igylgAtzfuhFbC{[\",\"name\":\"Laurel Place\",\"mode\":\"driving\",\"maneuver\":{\"location\":[-122.523579,37.975173],\"bearing_before\":195.0,\"bearing_after\":99.0,\"instruction\":\"Turn left onto Laurel Place\",\"type\":\"turn\",\"modifier\":\"left\"},\"voiceInstructions\":[{\"distanceAlongGeometry\":22.6,\"announcement\":\"Turn right onto Lincoln Avenue, then you will arrive at your destination\",\"ssmlAnnouncement\":\"\\u003cspeak\\u003e\\u003camazon:effect name\\u003d\\\"drc\\\"\\u003e\\u003cprosody rate\\u003d\\\"1.08\\\"\\u003eTurn right onto Lincoln Avenue, then you will arrive at your destination\\u003c/prosody\\u003e\\u003c/amazon:effect\\u003e\\u003c/speak\\u003e\"}],\"bannerInstructions\":[{\"distanceAlongGeometry\":41.2,\"primary\":{\"text\":\"Lincoln Avenue\",\"components\":[{\"text\":\"Lincoln Avenue\",\"type\":\"text\",\"abbr\":\"Lincoln Ave\",\"abbr_priority\":0}],\"type\":\"turn\",\"modifier\":\"right\"}}],\"driving_side\":\"right\",\"weight\":43.0,\"intersections\":[{\"location\":[-122.523579,37.975173],\"bearings\":[15,105,285],\"entry\":[false,true,true],\"in\":0,\"out\":1}]},{\"distance\":4.7,\"duration\":1.0,\"geometry\":\"ecylgAx}euhFpAZ\",\"name\":\"Lincoln Avenue\",\"mode\":\"driving\",\"maneuver\":{\"location\":[-122.523117,37.975107],\"bearing_before\":99.0,\"bearing_after\":194.0,\"instruction\":\"Turn right onto Lincoln Avenue\",\"type\":\"turn\",\"modifier\":\"right\"},\"voiceInstructions\":[{\"distanceAlongGeometry\":4.7,\"announcement\":\"You have arrived at your destination\",\"ssmlAnnouncement\":\"\\u003cspeak\\u003e\\u003camazon:effect name\\u003d\\\"drc\\\"\\u003e\\u003cprosody rate\\u003d\\\"1.08\\\"\\u003eYou have arrived at your destination\\u003c/prosody\\u003e\\u003c/amazon:effect\\u003e\\u003c/speak\\u003e\"}],\"bannerInstructions\":[{\"distanceAlongGeometry\":4.7,\"primary\":{\"text\":\"You have arrived\",\"components\":[{\"text\":\"You have arrived\",\"type\":\"text\"}],\"type\":\"arrive\",\"modifier\":\"straight\"}}],\"driving_side\":\"right\",\"weight\":1.0,\"intersections\":[{\"location\":[-122.523117,37.975107],\"bearings\":[15,105,195,285],\"entry\":[true,true,true,false],\"in\":3,\"out\":2}]},{\"distance\":0.0,\"duration\":0.0,\"geometry\":\"s`ylgAt~euhF\",\"name\":\"Lincoln Avenue\",\"mode\":\"driving\",\"maneuver\":{\"location\":[-122.523131,37.975066],\"bearing_before\":195.0,\"bearing_after\":0.0,\"instruction\":\"You have arrived at your destination\",\"type\":\"arrive\"},\"voiceInstructions\":[],\"bannerInstructions\":[],\"driving_side\":\"right\",\"weight\":0.0,\"intersections\":[{\"location\":[-122.523131,37.975066],\"bearings\":[15],\"entry\":[true],\"in\":0}]}],\"annotation\":{\"distance\":[21.030105037432428,41.16669115760234,4.722589365163041],\"congestion\":[$congestion]}}],\"routeOptions\":{\"baseUrl\":\"https://api.mapbox.com\",\"user\":\"mapbox\",\"profile\":\"driving-traffic\",\"coordinates\":[[-122.5237559,37.9754094],[-122.5231475,37.9750697]],\"alternatives\":true,\"language\":\"en\",\"continue_straight\":false,\"roundabout_exits\":false,\"geometries\":\"polyline6\",\"overview\":\"full\",\"steps\":true,\"annotations\":\"congestion,distance\",\"voice_instructions\":true,\"banner_instructions\":true,\"voice_units\":\"imperial\",\"access_token\":\"$tokenHere\",\"uuid\":\"ck9g2sbdk6pod7ynuece0r2yo\"},\"voiceLocale\":\"en-US\"}"
        return DirectionsRoute.fromJson(directionsRouteAsJson)
    }
}
