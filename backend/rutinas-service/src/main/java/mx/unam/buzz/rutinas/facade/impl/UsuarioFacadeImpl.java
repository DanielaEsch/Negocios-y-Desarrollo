package mx.unam.buzz.rutinas.facade.impl;

import lombok.RequiredArgsConstructor;
import mx.unam.buzz.rutinas.common.vo.IdRequestVO;
import mx.unam.buzz.rutinas.common.vo.RequestVO;
import mx.unam.buzz.rutinas.common.vo.ResponseVO;
import mx.unam.buzz.rutinas.common.vo.SingleResponseVO;
import mx.unam.buzz.rutinas.common.vo.usuario.CreateUsuarioRequestVO;
import mx.unam.buzz.rutinas.common.vo.usuario.FindListUsuarioRequestVO;
import mx.unam.buzz.rutinas.common.vo.usuario.UpdateAreasRequestVO;
import mx.unam.buzz.rutinas.common.vo.usuario.UpdatePerfilRequestVO;
import mx.unam.buzz.rutinas.common.vo.usuario.UsuarioResponseVO;
import mx.unam.buzz.rutinas.facade.IUsuarioFacade;
import mx.unam.buzz.rutinas.service.IUsuarioService;
import org.springframework.stereotype.Component;

import static mx.unam.buzz.rutinas.facade.impl.FacadeSupport.conSesion;

/**
 * Agrega la sesion y delega al servicio.
 */
@Component
@RequiredArgsConstructor
public class UsuarioFacadeImpl implements IUsuarioFacade {

    private final IUsuarioService usuarioService;

    @Override
    public SingleResponseVO<UsuarioResponseVO> findMe(RequestVO<Void> request) {
        return usuarioService.findMe(conSesion(request));
    }

    @Override
    public ResponseVO<UsuarioResponseVO> findList(RequestVO<FindListUsuarioRequestVO> request) {
        return usuarioService.findList(conSesion(request));
    }

    @Override
    public SingleResponseVO<UsuarioResponseVO> create(RequestVO<CreateUsuarioRequestVO> request) {
        return usuarioService.create(conSesion(request));
    }

    @Override
    public SingleResponseVO<UsuarioResponseVO> updatePerfil(RequestVO<UpdatePerfilRequestVO> request) {
        return usuarioService.updatePerfil(conSesion(request));
    }

    @Override
    public SingleResponseVO<UsuarioResponseVO> updateAreas(RequestVO<UpdateAreasRequestVO> request) {
        return usuarioService.updateAreas(conSesion(request));
    }

    @Override
    public SingleResponseVO<Boolean> delete(RequestVO<IdRequestVO> request) {
        return usuarioService.delete(conSesion(request));
    }
}
