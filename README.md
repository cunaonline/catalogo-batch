# catalogo-batch

Al ejecutarse con los parámetros correspondientes, el programa crea automaticamente 
(si no existen) las siguientes subcarpetas:
Subcarpeta con el nro de usuario (nnnn).
Dentro de esa carpeta:
  Subcarpeta "Logs" (donde genera registros de ejecución)
  Subcarpeta "Recibido" (Quedan los archivos recibidos con nombre
           ProveedorDelFicheroIni.xxx donde xxx depende del formato)

La estructura sería así (donde nnnn es el nro usuario):
C:\Catalogo\nnnn\Logs
C:\Catalogo\nnnn\Recibido
C:\Catalogo\nnnn\AEnviar

Al Catalogobatch.exe se le pueden pasar los siguientes parametros:

CatalogoBatch.exe l=user:pwd g=GLN [f=CSV]

Los parametros entre [] son opcionales.

l=nnnn:pwd ---> Nro usuario y contraseña para login a Rondanet (requerido)

g=GLN ---> GLN es el código EAN principal de la empresa, consultar en la
           pagina inicial de Rondanet si se desconoce éste dato 
           ("Lista de códigos de empresa"). 

f=CSV ---> formato en que se reciben los catálogos.
           Los formatos posibles a especificar son:
             XLS (planilla Excel)
             CSV (texto plano con campos delimitados por ^)
             DBF (archivo compatible con DBase/FoxPro)


Ejemplo: usuario: 0000 contraseña: test, gln=7737001000010 formato Excel
Catalogobatch.exe l=0000:test g=7737001000010 f=XLS

Ejemplo: dados de alta desde de enero del 2005, formato texto delimitado
Catalogobatch.exe l=0000:test  g=7737001000010  f=CSV  


Bajar catálogos
---------------

Para los usuarios que bajan los catálogos de los proveedores, se adjunta
además un archivo Catalogobatch.ini donde se deben especificar dos secciones:
1) Proveedores:
  Se ingresa una lista del codigo EAN principal de cada proveedor
  de los que se quiere bajar el catálogo. Se baja un archivo por proveedor.
  Se debe ingresar el codigo EAN del proveedor y el nombre.
  Ej 7737099000023=Una empresa S.A.
     (consultar con GS1 si se desconoce estos datos)
  El proveedor tiene que haber habilitado a la empresa a poder ver sus 
  productos en Rondanet.

2) Columnas:
  Lista de columnas visibles, contienen nombre del campo y descripcion. 
  Las columnas aparecen en el archivo que se baja en el mismo orden que 
  aparecen en el ini. El orden no se puede modificar pero si se pueden 
  definir que columnas bajar. Para que una columna NO aparezca en el 
  archivo ponerle un * delante.

El archivo ini debe estar en la misma carpeta que el programa.