# Configuración necesaria de autenticación para MimoPets

La app implementa acceso por correo y contraseña, recuperación de contraseña y el inicio OAuth con Google usando Supabase.

## URL de retorno Android

En Supabase Dashboard → Authentication → URL Configuration, agrega esta URL a **Redirect URLs**:

`mimopets://auth-callback`

La confirmación del correo y el restablecimiento de contraseña regresan a esa dirección.

## Activar Google

En Supabase Dashboard → Authentication → Providers → Google:
1. Activa Google.
2. Configura el OAuth Client ID y Client Secret creados en Google Cloud Console.
3. Guarda los cambios.
4. Verifica que la URL de retorno del proveedor en Google Cloud coincida exactamente con la URL de callback que muestra Supabase para el proyecto.

Si Google no está habilitado o las credenciales no son válidas, el botón abrirá el flujo pero el proveedor rechazará el acceso. No se deben colocar secretos de Google dentro de la aplicación Android.

## Datos que procesa la app

- Correo y credenciales: los gestiona Supabase Auth; la app no guarda la contraseña.
- Partida: mascota elegida, monedas, racha, nivel, habitación, accesorios y progreso.
- La partida se conserva localmente y, al iniciar sesión, se puede recuperar/guardar en la tabla `pet_saves`.

Antes de distribuir públicamente la aplicación, el responsable debe publicar una política de privacidad con un canal de contacto y un procedimiento para solicitudes de eliminación de cuenta/datos.
