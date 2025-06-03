package com.userjava.user.business.DTO;

import com.userjava.user.business.DTO.converter.UsuarioConverter;
import com.userjava.user.infrastructure.entity.Endereco;
import com.userjava.user.infrastructure.entity.Telefone;
import com.userjava.user.infrastructure.entity.Usuario;
import com.userjava.user.infrastructure.exceptions.ConflictException;
import com.userjava.user.infrastructure.exceptions.ResourceNotFoundException;
import com.userjava.user.infrastructure.repository.EnderecoRepository;
import com.userjava.user.infrastructure.repository.TelefoneRepository;
import com.userjava.user.infrastructure.repository.UsuarioRepository;
import com.userjava.user.infrastructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioConverter usuarioConverter;
    private final PasswordEncoder passwordEnconder;
    private final JwtUtil jwtUtil;
    private final EnderecoRepository enderecoRepository;
    private final TelefoneRepository telefoneRepository;

    public UsuarioDTO salvaUsuario(UsuarioDTO usuarioDTO){
        Usuario usuario = usuarioConverter.paraUsuario(usuarioDTO);
        usuarioDTO.setSenha(passwordEnconder.encode(usuarioDTO.getSenha()));
        return usuarioConverter.paraUsuarioDTO(usuarioRepository.save(usuario));
    }

    public void emailExiste(String email){
        try{
            boolean existe = verificaEmailExistente(email);
            if (existe){
                throw new ConflictException("Email já cadastrado " + email);
            }
        } catch (ConflictException e){
            throw new ConflictException("Email já cadastrado ", e.getCause());
        }
    }

    public boolean verificaEmailExistente(String email){
        return  usuarioRepository.existsByEmail(email);
    }

    public UsuarioDTO buscarUsuarioPorEmail(String email){
        try{
        return usuarioConverter.paraUsuarioDTO(
                usuarioRepository.findByEmail(email)
                        .orElseThrow(
                () -> new ResourceNotFoundException("Email não encontrado" + email)
                        )
        );
    }catch (ResourceNotFoundException e) {
         throw new ResourceNotFoundException("Email não encontrado " + email);
    }
 }


    public void deletaUsuarioPorEmail(String email){

            usuarioRepository.deleteByEmail(email);


    }

    public UsuarioDTO atualizaDadosUsuario(String token, UsuarioDTO dto){
        //Buscando o email do usuário atraves do token (tirar a obrigatoriedade do email)
       String email = jwtUtil.extrairEmailToken(token.substring(7));

       //Criptografia de senha
       dto.setSenha(dto.getSenha() != null ? passwordEnconder.encode(dto.getSenha()) : null);

       //Buscando os dados do usuário no banco de dados
       Usuario usuarioEntity = usuarioRepository.findByEmail(email).orElseThrow(() ->
                new ResourceNotFoundException("Email não localizado"));

       //Mesclando os dados que recebemos da requisição DTO com dados do banco de dados
       Usuario usuario = usuarioConverter.updateUsuario(dto, usuarioEntity);

       //Salvou os dados do usuário convertido e depois pegou o retorno e converteu para Usuario DTO
       return usuarioConverter.paraUsuarioDTO(usuarioRepository.save(usuario));
    }

    public EnderecoDTO atualizaEndereco(Long idEndereco, EnderecoDTO enderecoDTO){

        Endereco entity = enderecoRepository.findById(idEndereco).orElseThrow(() ->
                new ResourceNotFoundException("Id não encontrado " + idEndereco));

        Endereco endereco = usuarioConverter.updateEndereco(enderecoDTO, entity);

        return  usuarioConverter.paraEnderecoDTO(enderecoRepository.save(endereco));

    }

    public TelefoneDTO atualizaTelefone(Long idTelefone, TelefoneDTO dto){

        Telefone entity = telefoneRepository.findById(idTelefone).orElseThrow(() ->
                new ResourceNotFoundException("Id não encontrado " + idTelefone));

        Telefone telefone = usuarioConverter.updateTelefone(dto, entity);

        return  usuarioConverter.paraTelefoneDTO(telefoneRepository.save(telefone));
    }
}
