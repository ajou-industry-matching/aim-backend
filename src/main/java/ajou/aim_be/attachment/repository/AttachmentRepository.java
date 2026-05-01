package ajou.aim_be.attachment.repository;

import ajou.aim_be.attachment.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    List<Attachment> findByPost_PostIdOrderByDisplayOrderAscCreatedAtAsc(Long postId);

    List<Attachment> findByAttachmentIdIn(List<Long> attachmentIds);

    void deleteByPost_PostId(Long postId);

    List<Attachment> findByPost_PostIdOrderByDisplayOrderAsc(Long postId);

}