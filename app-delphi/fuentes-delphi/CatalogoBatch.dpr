{*
  Programa de consola usado por los usuarios para subir/bajar catalogos a Rondanet.

  El Catalogobatch usa la misma URL que el browser y los mismos parametros para
  hacer la consulta o subir archivos por lo que en el servidor no hay distinci?n
  si se accede desde el browser o el CatalogBatch. Se pasa el usuario y contrase?a
  para hacer un login temporal (no genera un registro de sesi?n en la tabla SES);
}
program CatalogoBatch;

{$APPTYPE CONSOLE}

uses
  SysUtils,
  Classes,
  IniFiles,
  SciZipFile,
  RnHTTP in '..\_comun\RnHTTP.pas';

const
  FILENAME_FORMAT: string[20] = 'yymmdd_hhnnsszzz'; /// Formato para generar los nombres de los archivos bajados

var
  CarpetaRecibido: shortstring; /// Carpete donde deja los archivos bajados
  CarpetaAEnviar: shortstring; /// Carpeta donde se dejan las planillas a subir
  CarpetaLogs: shortstring; /// Carpeta donde guarda los archivos de log
  Usuario: string[30]; /// Usuario para el login
  Contrasena: string[30]; /// Contrase?a para el login
  Gln2: string[30]; /// GLN principal de la empresa
  Formato: string[30]; /// Formato en que se quiere bajar (los mismos que se muestran en el web)
  Estado: string[30]; /// Si solo productos activos, solo no activos o todos (lo mismo que en el web)
  DeFecha: string[30]; /// A Partir de que fecha (de alta o baja seg?n Estado) (lo mismo que en el web)
  SoloConPrecio: string[30]; /// Si baja solo productos que tienen precio (lo mismo que en el web)
  Columnas: string; /// Campos que quiere incluir (lo mismo que en el web)
  LogStream: TFileStream; /// Archivo de log


{*
  Funcion que graba una entrada en el log
  @PARAM Texto Texto a agregar al log
}
procedure WriteLog(const Texto: string);
const
  CRLF: string[2] = #13#10;
begin
  if Assigned(LogStream) then
    begin
    LogStream.Write(Texto[1], Length(Texto));
    LogStream.Write(CRLF[1], Length(CRLF));
    end;
end;

{*
  Funcion que formatea un texto (agregandole fecha/hora) y lo muestra por pantalla
  (de consola) y lo agrega al log.
  @PARAM TextoMensaje Texto a mostrar en la pantalla y agregar al log
}
procedure AddMensaje(const TextoMensaje: string);
var
  vTextoMensaje: string;
begin
  vTextoMensaje := FormatDateTime('hh:nn:ss ', Now) + TextoMensaje;
  WriteLn(vTextoMensaje);
  WriteLog(vTextoMensaje);
end;

{*
  Carga los parametros de la linea de comandos
  Parametros: l=user:pwd g=GLN [f=CSV] [e=T] [d=dd.mm.aaaa]
}
procedure CargarParametros;
var
  i, j: Integer;
  MiParametro: string;
  MiOpcion: char;
begin
  // Parametros de linea
  Usuario := '';
  Contrasena := '';
  Gln2 := '';
  Formato := '';
  Estado := '';
  DeFecha := '';
  SoloConPrecio := '';
  for i := 1 to ParamCount do
    begin
    MiParametro := ParamStr(i);
    if (Length(MiParametro) < 3) then Continue;
    if (MiParametro[2] <> '=') then
      raise Exception.Create('Parametro no valido ' + MiParametro);
    MiOpcion := UpCase(MiParametro[1]);
    System.Delete(MiParametro, 1, 2);
    case MiOpcion of
      'L':begin
          j := Pos(':', MiParametro);
          if (j = 0) then
            raise Exception.Create('Se debe especificar "l=usuario:contrase?a"');
          Usuario := Copy(MiParametro,1,j-1);
          Contrasena := Copy(MiParametro,j+1,1000);
          end;
      'G':Gln2 := MiParametro;
      'F':Formato := UpperCase(MiParametro);
      'E':Estado := UpperCase(Copy(MiParametro,1,1));
      'D':DeFecha := StringReplace(MiParametro,'.','%2F',[rfReplaceAll]);
      'P':SoloConPrecio := UpperCase(Copy(MiParametro,1,1));
    else
      raise Exception.Create('Parametro no reconocible ' + MiParametro);
    end;
    end;
  if ((Length(Usuario) = 0) or (Length(Contrasena) = 0)) then
    raise Exception.Create('Se necesita parametro: l=usuario:contrase?a');
  if (Length(Gln2) = 0) then
    raise Exception.Create('Se necesita parametro: g=codigo EAN cliente');
  if (Length(Estado) = 0) then Estado := 'T';
  if (Length(Formato) = 0) then Formato := 'CSV';
  if (Length(SoloConPrecio) = 0) then SoloConPrecio := 'N';
end;

// Subcarpetas
//    Logs
//    Recibido
//    AEnviar
{*
  Inicializa variables con paths, crea subcarpetas si no existen (Logs, Recibido,
  AEnviar), crea o abre archivo de log (se crea 1 x d?a).
  @RETURN Si tuvo ?xito al inicializar
}
function Inicializar: Boolean;
var
  MiFileName, MiCarpetaBase, MiErrorFileName: shortstring;
begin
  Result := True;
  try
    MiCarpetaBase := IncludeTrailingPathDelimiter(ExtractFileDir(ParamStr(0)));
    MiErrorFileName := MiCarpetaBase + 'Error.txt';
    SysUtils.DeleteFile(MiErrorFileName);
    CargarParametros;
  except
    on E:Exception do
      begin
      //RnUtils.SaveString(E.Message, MiErrorFileName);
      Result := False;
      Exit;
      end;
  end;
  LogStream := nil;
  // Inicializa carpetas
  MiCarpetaBase := MiCarpetaBase + Usuario + '\';
  AddMensaje(MiCarpetaBase);
  CarpetaRecibido := MiCarpetaBase + 'Recibido\';
  CarpetaAEnviar  := MiCarpetaBase + 'AEnviar\';
  CarpetaLogs     := MiCarpetaBase + 'Logs\';
  // Crea carpetas si no existen
  SysUtils.ForceDirectories(CarpetaRecibido);
  SysUtils.ForceDirectories(CarpetaAEnviar);
  SysUtils.ForceDirectories(CarpetaLogs);
  // Abre o crea log exclusivo (no permite mas de un thread)
  MiFileName := CarpetaLogs +
                FormatDateTime('yyyy-mm-dd', Date) +
                '.txt';
  try
    if FileExists(MiFileName) then
      begin
      LogStream := TFileStream.Create(MiFileName, fmOpenReadWrite or fmShareExclusive);
      LogStream.Seek(0, soFromEnd);
      end
    else
      LogStream := TFileStream.Create(MiFileName, fmCreate or fmShareExclusive);
  except
    Result := False;
  end;
end;

{*
  Cierra archivo de log.
}
procedure Finalizar;
begin
  FreeAndNil(LogStream);
end;

{*
  Carga un TStringList con los GLNS de los proveedores y un string (global) con las
  columnas a incluir. Los datos los carga de un archivo ini que se debe encontrar
  en la misma carpeta y tener el mismo nombre que el programa.
  @RETURN TStringList con los GLNs de los proveedores de los que se quiere bajar el cat?logo
}
function CargarIni: TStringList;
var
  MiFileName: string;
  MiIniFile: TIniFile;
  MisColumnas: TStringList;
  i: Integer;
begin
  // Parametros de archivo ini
  MiFileName := Paramstr(0);
  MiFileName := Copy(MiFileName, 1, Length(MiFileName)-3) + 'ini';
  MiIniFile := TIniFile.Create(MiFileName);
  try
    Result := TStringList.Create;
    MisColumnas := TStringList.Create;
    try
      MiIniFile.ReadSection('Proveedores', Result);
      MiIniFile.ReadSection('Columnas', MisColumnas);
      Columnas := '';
      for i := 0 to MisColumnas.Count - 1 do
        if ((Copy(MisColumnas[i],1,1) <> '*') and (MisColumnas[i] <> '')) then
          Columnas := Columnas + '&columnas=' + MisColumnas[i];
    finally
      MisColumnas.Free;
    end;
  finally
    FreeAndNil(MiIniFile);
  end;
end;

{*
  Funci?n para obtener la URL de conexi?n, tiene en cuenta si est? en ambiente de
  prueba o no (o sea existe el archivo Edilocal.txt en la misma carpeta del
  programa)
  @RETURN URL
}
function GetUrl: string;
var
  vPath: string;
begin
  vPath := ExtractFilePath(GetModuleName(HInstance));
  if FileExists(vPath + 'Edilocal.txt') then
    Result := 'http://192.168.255.36'
  else
  if FileExists(vPath + 'Edilocal2.txt') then
    Result := 'http://192.168.255.244:8003'
  else
    Result := 'https://backup.rondanet.com/';
end;


{*
  Baja el catalogo de 1 proveedor
  @PARAM URL A donde se conecta (se arma dinamica dependiendo si existe Edilocal.txt)
  @PARAM Glnprov GLN del proveedor a bajar el catalogo
}
procedure Recibir1(const URL, Glnprov: string);
var
  vContenido, vFileName: string;
  vRespuesta: TMemoryStream;
  vFile : TFileStream;
  vZip: TZipFile;
begin
  // gln=7737001000007&glnprov=7737001000007&orden=DESCRIP&columnas=CPP&columnas=DESCRIP&columnas=MARCA&columnas=DIVISION&estado=A&filtro=&defecha=&comprimir=S&resultado=TXT&btnconsultar=Consultar
  AddMensaje('Recibiendo: ' + Glnprov);
  vContenido := 'LoginUsuario=' + Usuario +
                 '&LoginContrasena=' + Contrasena +
                 '&LoginTemporal=S' +
                 '&gln=' + Gln2 +
                 '&glnprov=' + Glnprov +
//                 '&orden=CPP' +
                 Columnas +
                 '&defecha=' + DeFecha +
                 '&resultado=' + Formato +
                 '&estado=' + Estado +
                 '&soloconprecio=' + SoloConPrecio +
                 '&comprimir=S';
  vRespuesta := TMemoryStream.Create;
  try
    // Recibe
    HTTP_SimplePost(URL,
                    vContenido,
                    'application/x-www-form-urlencoded',
                    'application/zip',
                    vRespuesta);

    // Descomprime
    vZip := TZipFile.Create;
    try
      vZip.LoadFromStream(vRespuesta);
      if (vZip.Count = 0) then
      begin
        vRespuesta.Position := 0;
        SetLength(vContenido, vRespuesta.Size);
        vRespuesta.Read(vContenido[1], Length(vContenido));
        AddMensaje(Copy(vContenido,1,255));
        Exit;
      end;
      AddMensaje('Descomprimiendo');
      vContenido := '';
      vFileName := vZip.Name[0];
      if (Length(vFileName) > 0)  then
        vContenido := vZip.Uncompressed[0];
    finally
      FreeAndNil(vZip);
    end;
    // Salva
    if (Length(vContenido) > 0) then
    begin
      vFileName := CarpetaRecibido +
                   Glnprov +
                   //FormatDatetime('yyyymmddhhnnsszzz', Now) +
                   ExtractFileExt(vFileName);
      AddMensaje('Guardando ' + vFileName);
      vFile := TFileStream.Create(vFileName, fmCreate or fmShareExclusive);
      try
        vFile.Write(vContenido[1], Length(vContenido));
      finally
        FreeAndNil(vFile);
      end;
    end;
  finally
    FreeAndNil(vRespuesta);
  end;
end;


{
POST /cgi-bin/Catalogo.exe/exportar3 HTTP/1.1
Accept: */*
Referer: http://192.168.255.36:801/cgi-bin/Catalogo.exe/consulta3
Accept-Language: es-uy
Content-Type: application/x-www-form-urlencoded
Accept-Encoding: gzip, deflate
User-Agent: Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1) ; .NET CLR 2.0.50727; MEGAUPLOAD 2.0)
Host: 192.168.255.36:801
Content-Length: 179
Connection: Keep-Alive
Cache-Control: no-cache
Cookie: RondaID=C658A9914E4043A281EEBD90F0167A4E

gln=7737001000007&glnprov=7737001000007&orden=DESCRIP&columnas=CPP&columnas=DESCRIP&columnas=MARCA&columnas=DIVISION&estado=A&filtro=&defecha=&comprimir=S&resultado=TXT&btnconsultar=Consultar
}
{*
  Baja los cat?logos del web. Hace loop y ejecuta Recibir1 por cada proveedor
}
procedure Recibir;
var
  i: Integer;
  vProveedores: TStringList;
begin
  vProveedores := CargarIni;
  try
    if (vProveedores.Count = 0) then
    begin
      AddMensaje('Nada para bajar: No hay Codigos de proveedores en ini');
      Exit;
    end;
    for i := 0 to vProveedores.Count - 1 do
    begin
      try
        AddMensaje('URL: '+GetUrl);
        Recibir1(GetUrl + '/cgi-bin/Catalogo.exe/exportar3', vProveedores[i]);
      except
        on E:Exception do
        begin
          AddMensaje('ERROR: ' + E.Message);
        end;
      end;
    end;
  finally
    FreeAndNil(vProveedores);
  end;
end;

{*
  Carga un archivo en un string en memoria.
  @PARAM FileName Nombre del archivo a cargar
  @RETURN Archivo cargado en string
}
function LoadFile(const FileName: string): string;
var
  vStream: TFileStream;
begin
  vStream := TFileStream.Create(FileName, fmOpenRead or fmShareDenyNone);
  try
    SetLength(Result, vStream.Size);
    vStream.Position := 0;
    vStream.Read(Result[1], Length(Result));
  finally
    vStream.Free;
  end;
end;

{
POST /cgi-bin/Catalogo.exe/actualizar.txt HTTP/1.1
Accept: */*
Referer: http://192.168.255.36:801/cgi-bin/Catalogo.exe/mantenim
Accept-Language: es-uy
Content-Type: multipart/form-data; boundary=---------------------------7d829ff902b8
Accept-Encoding: gzip, deflate
User-Agent: Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1) ; .NET CLR 2.0.50727)
Host: 192.168.255.36:801
Content-Length: 48019
Connection: Keep-Alive
Cache-Control: no-cache
Cookie: RondaID=58E66A276EE94DA6AB56BD5F32997D9B

-----------------------------7d829ff902b8
Content-Disposition: form-data; name="planilla"; filename="C:\Temp\lp.xls"
Content-Type: application/vnd.ms-excel

??ࡱ?
-----------------------------7d829ff902b8
Content-Disposition: form-data; name="subirp.x"

61
-----------------------------7d829ff902b8
Content-Disposition: form-data; name="subirp.y"

3
-----------------------------7d829ff902b8--
}

{*
  Sube planillas de productos, clientes y/o listas de precio.
}
procedure Enviar;
const
  BOUNDARY = '-----------------------------7d829ff902b8';
  CRLF = #13#10;
var
  vURL: string[100];
  vFile: TSearchRec;
  vContenido: string;
  vRespuesta: TStringStream;
begin
  vURL := GetUrl + '/cgi-bin/Catalogo.exe/actualizar.txt';
  try
    try
      if (SysUtils.FindFirst(CarpetaAEnviar + '*.xls', faAnyFile, vFile) <> 0) then
      begin
        AddMensaje('No hay archivos para enviar');
        Exit;
      end;
      repeat
        AddMensaje('Enviando ' + vFile.Name);
        vContenido :=

          '--' + BOUNDARY + CRLF +
          'Content-Disposition: form-data; name="LoginUsuario"' + CRLF + CRLF +
          Usuario + CRLF +

          '--' + BOUNDARY + CRLF +
          'Content-Disposition: form-data; name="LoginContrasena"' + CRLF + CRLF +
          Contrasena + CRLF +

          '--' + BOUNDARY + CRLF +
          'Content-Disposition: form-data; name="LoginTemporal"' + CRLF + CRLF +
          'S' + CRLF +

          '--' + BOUNDARY + CRLF +
          'Content-Disposition: form-data; name="planilla"; filename="' + CarpetaAEnviar + vFile.Name + '"' + CRLF +
          'Content-Type: application/vnd.ms-excel' + CRLF + CRLF +
          LoadFile(CarpetaAEnviar + vFile.Name) + CRLF +

          '--' + BOUNDARY + '--';
        try
          vRespuesta := TStringStream.Create('');
          try
            HTTP_SimplePost(vURL,
                            vContenido,
                            'multipart/form-data; boundary=' + BOUNDARY,
                            'text/plain',
                            vRespuesta);
            vContenido := vRespuesta.DataString;
            vRespuesta.Size := 0;
            if (Pos('siguiente error:', vContenido) > 0) then
            begin
              AddMensaje('Se produjo error');
            end
            else
            begin
              AddMensaje('Enviado Ok');
              SysUtils.DeleteFile(CarpetaAEnviar + vFile.Name);
            end;
            WriteLog(' ');
            WriteLog(vContenido);
            WriteLog(' ');
          finally
            vRespuesta.Free;
          end;
        except
          on E:Exception do AddMensaje(E.Message);
        end;
      until (SysUtils.FindNext(vFile) <> 0);
    finally
      SysUtils.FindClose(vFile);
    end;
  except
    on E:Exception do AddMensaje(E.Message);
  end;
end;

begin
  if (not Inicializar) then Exit;
  try
    try
      Recibir;
      Enviar;
      AddMensaje('Fin');
      WriteLog(' ');
    except
      on E:Exception do AddMensaje(E.Message);
    end;
  finally
    Finalizar;
  end;
//  ReadLn(Texto);
end.
