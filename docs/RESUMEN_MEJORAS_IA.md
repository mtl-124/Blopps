# Resumen de Mejoras con IA (Fase 2) - Blopps

Este documento detalla las tres grandes mejoras incorporadas en la Fase 2 de la aplicación **Blopps**.

---

## 1. Creación de publicaciones de usuarios con búsqueda en TMDB

### Objetivo
Permitir que los usuarios autenticados creen sus propias reseñas seleccionando películas oficiales de la base de datos de TMDB y publicándolas organizadas por comunidades en Cloud Firestore.

### Archivos Creados
- `domain/model/PeliculaBusqueda.kt`
- `domain/model/PublicacionUsuario.kt`
- `data/repository/PeliculaRepository.kt`
- `data/repository/PeliculaRepositoryImpl.kt`
- `data/repository/PublicacionUsuarioRepository.kt`
- `data/repository/FirestorePublicacionUsuarioRepository.kt`
- `ui/crear/CrearPublicacionViewModel.kt`
- `ui/crear/CrearPublicacionScreen.kt`
- `ui/components/CalificacionEstrellas.kt`
- Pruebas unitarias correspondientes.

### Archivos Modificados
- `data/remote/MovieApiService.kt`
- `data/remote/dto/MovieDto.kt`
- `data/mapper/Mappers.kt`
- `AppContainer.kt`
- `AppNav.kt`
- `ComunidadesScreen.kt`

### Decisiones Técnicas
- **Separación de Room y Firestore:** `PublicacionUsuario` se modela independientemente de la entidad Room de JSONPlaceholder, almacenándose exclusivamente en Firestore (`publicaciones_usuarios`).
- **Búsqueda con debounce:** Se implementó un operador `debounce(500)` para optimizar las peticiones al escribir en el buscador de películas.
- **Degradación elegante sin TMDB:** Si `TMDB_API_KEY` no está configurada en `local.properties`, la app degrada la funcionalidad mostrando un aviso descriptivo sin generar crasheos.

### Limitaciones Conocidas
- La búsqueda requiere conectividad a internet activa y clave de API válida de TMDB.

---

## 2. Sistema social en tiempo real (Comentarios, Likes y Votos)

### Objetivo
Dotar a las publicaciones de usuarios de interactividad social completa: hilos de comentarios con respuestas de un solo nivel, "me gusta" instantáneos y cálculo dinámico de calificación promedio de la comunidad.

### Archivos Creados
- `domain/model/Comentario.kt`
- `domain/model/ResumenSocial.kt`
- `domain/model/HiloComentario.kt`
- `data/repository/SocialRepository.kt`
- `data/repository/FirestoreSocialRepository.kt`
- `ui/detalle/Hilos.kt`
- `ui/detalle/SocialViewModel.kt`
- `ui/detalle/SocialSeccion.kt`
- `firestore.rules`
- Pruebas unitarias correspondientes.

### Archivos Modificados
- `AppContainer.kt`
- `DetallePublicacionUsuarioScreen.kt`
- `AppNav.kt`

### Decisiones Técnicas
- **Aplanamiento de hilos:** Las respuestas a respuestas se aplanan automáticamente para apuntar a la raíz del comentario, evitando anidamientos complejos.
- **Actualización optimista:** Los likes y votos actualizan la interfaz local inmediatamente y se revierten automáticamente si la operación remota falla.
- **Manejo de huérfanos:** La lógica pura `armarHilos` promueve respuestas cuyos padres fueron eliminados para evitar pérdida visual de datos.

### Limitaciones Conocidas
- El promedio de calificación y los totales se calculan en cliente combinando los streams de Firestore.
- No hay paginación de comentarios en servidor (se limita a mostrar los 50 más recientes con opción de ver más en cliente).

---

## 3. Rediseño visual (UI/UX) y animaciones fluidas

### Objetivo
Elevar la calidad visual de la aplicación aplicando principios avanzados de Material 3, tipografías mejoradas, tarjetas inmersivas y animaciones coordinadas para la navegación y las interacciones.

### Archivos Creados
- `ui/theme/Espaciado.kt`
- `ui/components/Efectos.kt`
- `ui/components/CargandoShimmer.kt`

### Archivos Modificados
- `ui/theme/Color.kt`
- `ui/theme/Theme.kt`
- `ui/theme/Type.kt`
- `ui/components/PublicacionCard.kt`
- `ui/components/PublicacionUsuarioCard.kt`
- `ui/components/CalificacionEstrellas.kt`
- `ComunidadesScreen.kt`
- `FavoritosScreen.kt`
- `CrearPublicacionScreen.kt`
- `AppNav.kt`

### Decisiones Técnicas
- **Efecto Shimmer:** Se implementó un indicador de carga con gradiente animado en bucle (`infiniteRepeatable`) en lugar de indicadores circulares estáticos.
- **Transiciones de Navegación:** Se configuraron transiciones personalizadas con `slideInHorizontally` y `fadeIn` para pantallas secundarias.
- **Ocultamiento de FAB al hacer scroll:** Uso de `derivedStateOf` sobre el estado del `LazyListState` para evitar recomposiciones innecesarias al deslizar listas.

### Limitaciones Conocidas
- Las animaciones respetan los componentes estándar de Compose y están optimizadas para ejecutarse por debajo de 350 ms.

---

## Mensajes de Commit Sugeridos

1. `feat(tmdb): integrar buscador de películas y repositorio en capa de datos`
2. `feat(firestore): implementar modelo y repositorio Firestore para publicaciones de usuarios`
3. `feat(ui): crear pantalla y flujo de creación de publicaciones con TMDB`
4. `feat(social): integrar repositorio y UI para comentarios, likes y votos en tiempo real`
5. `refactor(hilos): añadir lógica de armado de hilos de comentarios y manejo de huérfanos`
6. `style(theme): mejorar paleta Material 3, tipografía y constantes de espaciado`
7. `anim(ux): incorporar animaciones de navegación, shimmer de carga y efectos táctiles`
