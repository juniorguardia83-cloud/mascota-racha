# Configuración necesaria de autenticación para MimoPets

La app implementa acceso por correo y contraseña, recuperación de contraseña y el inicio OAuth con Google usando Supabase.

## URL de retorno Android

En Supabase Dashboard → Authentication → URL Configuration, agrega esta URL a **Redirect URLs** y guarda los cambios:

`mimopets://auth-callback`

La confirmación del correo y el restablecimiento de contraseña regresan a esa dirección.

## Activar Google

En Supabase Dashboard → Authentication → Providers → Google:
1. Activa Google.
2. Configura el OAuth Client ID y Client Secret creados en Google Cloud Console.
3. En Google Cloud Console → APIs & Services → Credentials → OAuth 2.0 Client ID (tipo Web), añade como **Authorized redirect URI** la URL exacta que muestra Supabase para el callback del proveedor. Tiene este formato: `https://TU-PROYECTO.supabase.co/auth/v1/callback` (usa la URL real del panel, no escribas literalmente el ejemplo).
4. Guarda los cambios y espera unos minutos para que Google propague la configuración.

Si Google no está habilitado o las credenciales no son válidas, el botón abrirá el flujo pero el proveedor rechazará el acceso. No se deben colocar secretos de Google dentro de la aplicación Android.

## Datos que procesa la app

- Correo y credenciales: los gestiona Supabase Auth; la app no guarda la contraseña.
- Partida: mascota elegida, monedas, racha, nivel, habitación, accesorios y progreso.
- La partida se conserva localmente y, al iniciar sesión, se puede recuperar/guardar en la tabla `pet_saves`.

Antes de distribuir públicamente la aplicación, el responsable debe publicar una política de privacidad con un canal de contacto y un procedimiento para solicitudes de eliminación de cuenta/datos.
