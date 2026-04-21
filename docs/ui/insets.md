# Arquitectura de insets y safe area en Kippo

## Regla de oro

> **Ninguna pantalla aplica `windowInsetsPadding`, `statusBarsPadding`,
> `navigationBarsPadding` ni usa `Scaffold` de Material3 directamente.
> Siempre se usa `KippoScaffold`.**

`KippoScaffold` (en `ui/components/KippoScaffold.kt`) es la única vía soportada
para construir una pantalla. Garantiza que el contenido respete:

- **Status bar** (barra superior).
- **Barra de navegación** — tanto la gestual como la de botones táctiles en
  Xiaomi / MIUI.
- **Display cutout** — perforación de cámara frontal (Redmi Note, etc.).

Los insets se consumen automáticamente y se exponen al `content` como
`PaddingValues`. **Es obligatorio** propagarlos:

```kotlin
KippoScaffold(topBar = { ... }, bottomBar = { ... }) { padding ->
    Column(Modifier.padding(padding).fillMaxSize()) { ... }
    // o para listas:
    LazyColumn(contentPadding = padding) { ... }
}
```

## Componentes del sistema

| Componente | Archivo | Qué hace |
|---|---|---|
| `KippoTheme` | `ui/theme/KippoTheme.kt` | Envuelve `MaterialTheme` + ajusta apariencia de iconos de system bars según luminancia del fondo. |
| `KippoScaffold` | `ui/components/KippoScaffold.kt` | `Scaffold` corporativo con `contentWindowInsets = statusBars + navigationBars + displayCutout`. |
| `KippoBottomBar` | `ui/components/KippoBottomBar.kt` | Consume `WindowInsets.navigationBars` internamente. |
| `KippoTopBar` (específico de HomeScreen) | `ui/HomeScreen.kt` | Consume `WindowInsets.statusBars.union(displayCutout)`. |
| `ToastHost` | `ui/components/toast/ToastHost.kt` | Overlay global; consume `statusBars.union(displayCutout)`. |

## Edge-to-edge

`MainActivity` llama a `enableEdgeToEdge()` antes de `setContent`. El manifiesto
declara `windowLayoutInDisplayCutoutMode="shortEdges"` para permitir dibujar
detrás del cutout sin ser recortado. `windowSoftInputMode="adjustResize"`
asegura que el layout se redimensione al abrir el teclado.

## Diálogos y bottom sheets

### `AlertDialog` de Material3
Maneja IME y safe area por defecto; no requiere tratamiento especial para los
casos actuales.

### `ModalBottomSheet`
Debe declararse con `contentWindowInsets = { WindowInsets.navigationBars }`
para que el contenido no quede bajo la barra de navegación:

```kotlin
ModalBottomSheet(
    onDismissRequest = onDismiss,
    contentWindowInsets = { WindowInsets.navigationBars }
) { ... }
```

## Formularios con teclado

Pantallas con `OutlinedTextField` y scroll (ej. `LoginScreen`, `RegisterScreen`)
deben añadir `Modifier.imePadding()` al contenedor scrollable para que el campo
activo quede por encima del teclado:

```kotlin
KippoScaffold { padding ->
    Column(
        Modifier
            .fillMaxSize()
            .padding(padding)
            .imePadding()
            .verticalScroll(rememberScrollState())
    ) { ... }
}
```

## Checklist al añadir una pantalla nueva

- [ ] Usa `KippoScaffold`, no `Scaffold` de Material3 ni `Box`/`Column` raíz.
- [ ] El `content` propaga los `PaddingValues` recibidos al contenedor
      principal (`Modifier.padding(padding)` o `LazyColumn#contentPadding`).
- [ ] Si hay formularios con teclado, añadir `Modifier.imePadding()`.
- [ ] Si es un `ModalBottomSheet`, añadir
      `contentWindowInsets = { WindowInsets.navigationBars }`.
- [ ] Verificar en emulador con gesture nav **y** en dispositivo Xiaomi físico
      (MIUI con botones táctiles inferiores).
- [ ] Verificar en dispositivo con cutout (Pixel 3 XL o Redmi con perforación)
      en orientación vertical y horizontal.

## Cambios recientes

La app migró a una arquitectura de insets centralizada. Lo que NO debe hacerse:

- ❌ Aplicar `Modifier.windowInsetsPadding(WindowInsets.safeDrawing)` en el
  contenedor raíz de `MainActivity` — recorta los fondos y rompe edge-to-edge.
- ❌ Usar `Scaffold` de Material3 directamente en una pantalla nueva.
- ❌ Hardcodear `padding(top = 24.dp)` para "empujar" contenido fuera de la
  status bar.
