package org.backend.domain.batch.job.memberfeature.reader;

import lombok.RequiredArgsConstructor;
import org.backend.domain.member.entity.Member;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JpaPagingItemReader;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class ChunkMemberReader implements ItemReader<List<Member>>, ItemStream {

    private final JpaPagingItemReader<Member> delegate;
    private final int chunkSize;
    private boolean exhausted = false;

    //  Step 시작 시 delegate의 EntityManager 등 자원 초기화
    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        exhausted = false;
        delegate.open(executionContext);
    }

    //  Step 완료/실패 시 자원 정리
    @Override
    public void close() throws ItemStreamException {
        delegate.close();
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        delegate.update(executionContext);
    }

    @Override
    public synchronized List<Member> read() throws Exception {
        if (exhausted) return null;

        List<Member> chunk = new ArrayList<>(chunkSize);
        for (int i = 0; i < chunkSize; i++) {
            Member member = delegate.read();
            if (member == null) {
                exhausted = true;
                break;
            }
            chunk.add(member);
        }
        return chunk.isEmpty() ? null : chunk;
    }
}