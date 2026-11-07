package mx.unam.buzz.rutinas.service;

import mx.unam.buzz.rutinas.common.vo.IdRequestVO;
import mx.unam.buzz.rutinas.common.vo.RequestVO;
import mx.unam.buzz.rutinas.common.vo.ResponseVO;
import mx.unam.buzz.rutinas.common.vo.SingleResponseVO;
import mx.unam.buzz.rutinas.common.vo.usuario.CreateUsuarioRequestVO;
import mx.unam.buzz.rutinas.common.vo.usuario.FindListUsuarioRequestVO;
import mx.unam.buzz.rutinas.common.vo.usuario.UpdateAreasRequestVO;
import mx.unam.buzz.rutinas.common.vo.usuario.UpdatePerfilRequestVO;
import mx.unam.buzz.rutinas.common.vo.usuario.UsuarioResponseVO;
import mx.unam.buzz.rutinas.modelo.entity.UsuarioDO;

public interface IUsuarioService {

    SingleResponseVO<UsuarioResponseVO> findMe(RequestVO<Void> request);

    ResponseVO<UsuarioResponseVO> findList(RequestVO<FindListUsuarioRequestVO> request);

    SingleResponseVO<UsuarioResponseVO> create(RequestVO<CreateUsuarioRequestVO> request);

    SingleResponseVO<UsuarioResponseVO> updatePerfil(RequestVO<UpdatePerfilRequestVO> request);

    SingleResponseVO<UsuarioResponseVO> updateAreas(RequestVO<UpdateAreasRequestVO> request);

    SingleResponseVO<Boolean> delete(RequestVO<IdRequestVO> request);

    /** Salida publica de un usuario (la usa tambien el login). */
    UsuarioResponseVO toResponse(UsuarioDO entity);
}
