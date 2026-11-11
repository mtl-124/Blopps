# Guía de Pruebas Manuales - Blopps (Fase 2)

| Código | Caso | Pasos | Resultado Esperado |
|:---|:---|:---|:---|
| PM-01 | Crear publicación completa | 1. Tocar el FAB en Comunidades.<br>2. Seleccionar comunidad.<br>3. Buscar y elegir película de TMDB.<br>4. Llenar título, reseña y estrellas.<br>5. Tocar "Publicar". | Se publica exitosamente en Firestore y aparece de inmediato en la lista de la comunidad. |
| PM-02 | Crear publicación sin conexión | 1. Desactivar internet.<br>2. Intentar publicar.<br>3. Verificar manejo de errores. | Se muestra mensaje de conectividad / reintento controlado sin crashear. |
| PM-03 | Búsqueda TMDB con y sin API Key | 1. Configurar o quitar `TMDB_API_KEY` en `local.properties`.<br>2. Intentar buscar películas en pantalla de creación. | Con clave busca y lista resultados; sin clave muestra aviso de que la búsqueda no está disponible sin romper la app. |
| PM-04 | Elegir y cambiar película | 1. Buscar película.<br>2. Seleccionarla.<br>3. Tocar "Cambiar" en la tarjeta elegida. | Se limpia la selección y permite buscar otra película distinta. |
| PM-05 | Validaciones de campos | 1. Dejar campos vacíos o con longitud inválida.<br>2. Tocar "Publicar". | Muestra mensajes exactos de validación (título 3..100, reseña 10..1000, calificación 1..5). |
| PM-06 | Sincronización en tiempo real | 1. Abrir la app en dos dispositivos con la misma comunidad.<br>2. Crear una publicación en uno. | La publicación aparece instantáneamente en el segundo dispositivo sin recargar manualmente. |
| PM-07 | Apertura de detalle de usuario | 1. Tocar una tarjeta de PublicacionUsuarioCard. | Abre la pantalla de detalle mostrando póster grande, metadatos, autor, tiempo relativo y texto completo. |
| PM-08 | Eliminación por autor o admin | 1. Entrar al detalle de una publicación propia o como admin.<br>2. Tocar icono de papelera y confirmar. | La publicación se elimina de Firestore y regresa a la pantalla anterior. |
| PM-09 | Usuario sin permisos no ve botón eliminar | 1. Entrar al detalle de una publicación de otro usuario siendo un usuario estándar (no admin). | El botón/icono de eliminar no se muestra en la barra superior. |
| PM-10 | Sistema de comentarios e hilos | 1. Escribir un comentario raíz.<br>2. Tocar "Responder" en un comentario y enviar.<br>3. Eliminar comentario propio. | Los comentarios se ordenan en hilos (respuestas indentadas), se pueden eliminar y actualizan el contador. |
| PM-11 | Likes en tiempo real | 1. Tocar el botón de corazón ("Me gusta").<br>2. Comprobar actualización de contador. | El contador de likes se actualiza instantáneamente con actualización optimista y persistencia en Firestore. |
| PM-12 | Votación y promedio | 1. Elegir estrellas en el bloque de calificación promedio.<br>2. Comprobar cambio de promedio y total de votos. | El promedio con 1 decimal y el conteo de votos se recalculan de inmediato en vivo. |
| PM-13 | Rotación de pantalla | 1. Rotar la pantalla estando en el formulario de creación o en detalle. | Los datos se preservan correctamente gracias al ViewModel y StateFlow. |
| PM-14 | Modo claro y oscuro | 1. Cambiar el tema del dispositivo entre claro y oscuro. | Todos los textos, superficies y tarjetas mantienen contraste accesible (WCAG AA). |
| PM-15 | Animaciones de navegación y FAB | 1. Navegar entre pantallas secundarias y hacer scroll en la lista. | Las transiciones de pantalla son fluidas y el FAB se oculta/reaparece al hacer scroll vertical. |
