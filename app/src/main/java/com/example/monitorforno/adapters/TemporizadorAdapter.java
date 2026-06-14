package com.example.monitorforno.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.monitorforno.R;
import com.example.monitorforno.models.Temporizador;

import java.util.List;

public class TemporizadorAdapter
        extends RecyclerView.Adapter<TemporizadorAdapter.ViewHolder> {

    public interface OnTemporizadorRemovidoListener {
        void onTemporizadorRemovido();
    }

    private final List<Temporizador> temporizadores;

    private final OnTemporizadorRemovidoListener listener;

    public TemporizadorAdapter(
            List<Temporizador> temporizadores,
            OnTemporizadorRemovidoListener listener) {

        this.temporizadores = temporizadores;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(
                parent.getContext()
        ).inflate(
                R.layout.item_temporizador,
                parent,
                false
        );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        Temporizador temporizador =
                temporizadores.get(position);

        holder.txtHorario.setText(
                temporizador.getHorario()
        );

        holder.imgExcluir.setOnClickListener(v -> {

            int posicaoAtual =
                    holder.getAdapterPosition();

            if (posicaoAtual == RecyclerView.NO_POSITION) {
                return;
            }

            temporizadores.remove(
                    posicaoAtual
            );

            notifyItemRemoved(
                    posicaoAtual
            );

            notifyItemRangeChanged(
                    posicaoAtual,
                    temporizadores.size()
            );

            listener.onTemporizadorRemovido();

        });
    }

    @Override
    public int getItemCount() {
        return temporizadores.size();
    }

    static class ViewHolder
            extends RecyclerView.ViewHolder {

        TextView txtHorario;
        ImageView imgExcluir;

        public ViewHolder(
                @NonNull View itemView) {

            super(itemView);

            txtHorario =
                    itemView.findViewById(
                            R.id.txtHorarioTemporizador
                    );

            imgExcluir =
                    itemView.findViewById(
                            R.id.imgExcluir
                    );
        }
    }
}